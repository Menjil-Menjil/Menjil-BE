package seoultech.capstone.menjil.domain.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignInServiceRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignUpServiceRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.response.SignInResponse;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static seoultech.capstone.menjil.global.exception.ErrorCode.PROVIDER_NOT_ALLOWED;
import static seoultech.capstone.menjil.global.exception.ErrorIntValue.USER_ALREADY_EXISTED;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.SUCCESS;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final AwsS3Handler awsS3Handler;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final long refreshTokenExpiresIn = 14;
    private final int AWS_URL_DURATION = 7;
    private static final String defaultImgUrl = "profile/default.png";

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 회원가입 전, 유저가 이미 db에 존재하는지 조회.
     */
    @Transactional(readOnly = true)
    public int findUserInDb(String email, String provider) {
        Optional<User> user = userRepository.findUserByEmailAndProvider(email, provider);
        return checkIfUserInDb(user) ? USER_ALREADY_EXISTED.getValue() : SUCCESS.getValue();
    }

    /**
     * 닉네임 중복 조회
     */
    @Transactional(readOnly = true)
    public int findNicknameInDb(String nickname) {
        Optional<User> user = userRepository.findUserByNickname(nickname);
        return checkIfUserInDb(user) ? USER_ALREADY_EXISTED.getValue() : SUCCESS.getValue();
    }

    /**
     * 회원가입 로직 수행
     */
    @Transactional
    public void signUp(SignUpServiceRequest request) {
        // SignUpRequestDto -> User Entity 변환
        User user = request.toUserEntity();

        // 기존에 중복된 유저가 있는 지 조회
        // 이 부분은 의미없다. 처음 가입할 때 유저 확인 후 redirect 처리 하므로.
        /*List<User> userInDb = userRepository.findUserByEmailAndProvider(user.getEmail(), user.getProvider());
        if (userInDb.size() > 0) {
            throw new CustomException(ErrorCode.USER_DUPLICATED);
        }*/

        // 혹시 클라이언트에서 닉네임 중복 검증을 놓친 경우 확인
        User nicknameExistsInDb = userRepository.findUserByNickname(user.getNickname())
                .orElse(null);
        if (nicknameExistsInDb != null) {
            // 이 부분은, 컨트롤러가 아닌 서비스에서 처리하는 것이 더 바람직할 것으로 보임.
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTED);
        }

        // Set AWS S3 default image url in user
        user.setImgUrl(defaultImgUrl);

        // save in db
        saveUser(user);
    }

    /**
     * 로그인 처리
     */
    @Transactional
    public SignInResponse signIn(SignInServiceRequest request) {
        String email = request.getEmail();
        String provider = request.getProvider();

        if (!provider.equals("google") && !provider.equals("kakao")) {
            throw new CustomException(PROVIDER_NOT_ALLOWED);
        }

        Optional<User> userInDb = userRepository.findUserByEmailAndProvider(email, provider);
        if (checkIfUserInDb(userInDb)) {
            User user = userInDb.get();

            // Access, Refresh Token 생성
            LocalDateTime currentDateTime = LocalDateTime.now();
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), currentDateTime);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), currentDateTime);

            // RefreshToken은 db에 저장.
            Timestamp expiryDate = Timestamp.valueOf(currentDateTime.plusDays(refreshTokenExpiresIn)); // 만료 날짜는 +14일
            RefreshToken rfEntity = RefreshToken.builder()
                    .id(null)
                    .userId(user)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build();

            Optional<RefreshToken> refreshTokenExistsInDb = tokenRepository.findRefreshTokenByUserId(user);
            if (refreshTokenExistsInDb.isPresent()) {
                // 기존에 로그인을 해서 db 에 데이터가 존재하는 경우, Update
                // status 의 값은 1
                int status = tokenRepository.updateRefreshToken(user, refreshToken, expiryDate);

            } else {
                // save in db
                try {
                    tokenRepository.save(rfEntity);
                } catch (RuntimeException e) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            }

            // Created 응답과 함께 Access, Refresh token, 그 외 사용자 정보를 담아서 클라이언트에게 전달
            return SignInResponse.of(accessToken, refreshToken, user.getNickname(),
                    user.getSchool(), user.getMajor(),
                    String.valueOf(awsS3Handler.generatePresignedUrl(BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));
        } else {
            throw new CustomException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    private boolean checkIfUserInDb(Optional<User> user) {
        return user.isPresent();
    }

    private void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (RuntimeException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
