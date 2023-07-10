package seoultech.capstone.menjil.domain.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequestDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignInResponseDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignUpCheckUserDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignUpResponseDto;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final HttpServletResponse response;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final long refreshTokenExpiresIn = 14;

    /**
     * 회원가입 전, 유저가 이미 db에 존재하는지 조회.
     *
     * @param email
     * @param provider
     */
    public SignUpCheckUserDto checkUserExistsInDb(String email, String provider) throws IOException {
        List<User> userInDb = userRepository.findUserByEmailAndProvider(email, provider);

        if (userInDb.size() > 0) {
            throw new CustomException(ErrorCode.USER_DUPLICATED);
        } else {
            return SignUpCheckUserDto.builder()
                    .status(200)
                    .message("회원가입이 가능한 이메일입니다")
                    .build();
        }
    }

    /**
     * 닉네임 중복 조회
     */
    @Transactional(readOnly = true)
    public String checkNicknameDuplication(String nickname) {
        User nicknameExistsInDb = userRepository.findUserByNickname(nickname)
                .orElse(null);
        if (nicknameExistsInDb != null) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
        return "Nickname is available";
    }

    /**
     * 회원가입 로직 수행
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        // SignUpRequestDto -> User Entity 변환
        User user = requestDto.toUser();

        // 기존에 중복된 유저가 있는 지 조회
        // 이 부분은 의미없다. 처음 가입할 때 유저 확인 후 redirect 처리 하므로.
        /*
        List<User> userInDb = userRepository.findUserByEmailAndProvider(user.getEmail(), user.getProvider());
        if (userInDb.size() > 0) {
            throw new CustomException(ErrorCode.USER_DUPLICATED);
        }
         */

        // 혹시 클라이언트에서 닉네임 중복 검증을 놓친 경우 확인
        User nicknameExistsInDb = userRepository.findUserByNickname(user.getNickname())
                .orElse(null);

        if (nicknameExistsInDb != null) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // db에 저장
        userRepository.save(user);

        // User Entity -> UserSignupResponseDto
        return new SignUpResponseDto(user);
    }

    /**
     * 로그인 처리
     */
    @Transactional
    public SignInResponseDto signIn(String email, String provider) {
        List<User> userInDb = userRepository.findUserByEmailAndProvider(email, provider);

        if (userInDb.size() > 0) {
            User user = userInDb.get(0);

            // Access, Refresh Token 생성
            LocalDateTime currentDateTime = LocalDateTime.now();
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), currentDateTime);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), currentDateTime);

            // RefreshToken 은 db 에 저장.
            Timestamp expiryDate = Timestamp.valueOf(currentDateTime.plusDays(refreshTokenExpiresIn)); // 만료 날짜는 +14일
            RefreshToken rfEntity = new RefreshToken(null, user, refreshToken, expiryDate);

            Optional<RefreshToken> refreshTokenExistsInDb = tokenRepository.findRefreshTokenByUserId(user);
            if (refreshTokenExistsInDb.isPresent()) {
                // 기존에 로그인을 해서 db 에 데이터가 존재하는 경우, Update
                // status 의 값은 1
                int status = tokenRepository.updateRefreshToken(user, refreshToken, expiryDate);

            } else {
                tokenRepository.save(rfEntity);
            }

            // Created 응답과 함께 Access, Refresh token 발급
            return SignInResponseDto.builder()
                    .status(201)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            throw new CustomException(ErrorCode.USER_NOT_EXISTED);
        }
    }

}
