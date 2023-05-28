package seoultech.capstone.menjil.domain.auth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.application.handler.GoogleOAuthHandler;
import seoultech.capstone.menjil.domain.auth.application.handler.KaKaoOauthHandler;
import seoultech.capstone.menjil.domain.auth.domain.SocialLoginType;
import seoultech.capstone.menjil.domain.auth.dto.*;
import seoultech.capstone.menjil.domain.auth.dto.response.OAuthUserResponseDto;
import seoultech.capstone.menjil.domain.auth.exception.CustomAuthException;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static seoultech.capstone.menjil.global.common.JwtUtils.getJwtSecretKey;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {
    private final GoogleOAuthHandler googleOAuthHandler;
    private final KaKaoOauthHandler kaKaoOauthHandler;
    private final HttpServletResponse response;
    private final UserRepository userRepository;

    public void requestRedirectURL(SocialLoginType socialLoginType) {
        String redirectURL = "";
        switch (socialLoginType) {
            case GOOGLE:
                redirectURL = googleOAuthHandler.getOauthRedirectURL();
                break;
            case KAKAO:
                redirectURL = kaKaoOauthHandler.getOauthRedirectURL();
                break;
            default:
                throw new IllegalArgumentException("알 수 없는 소셜 로그인 형식입니다.");
        }
        try {
            response.sendRedirect(redirectURL);
        } catch (IOException e) {
            log.error("redirect error of request");
            e.printStackTrace();
        }
    }

    public Object oAuthLogin(SocialLoginType socialLoginType, String code) throws JsonProcessingException {
        switch (socialLoginType) {
            case GOOGLE: {
                // 구글로 code 를 보내 액세스 토큰이 담긴 응답 객체를 받아온다.
                ResponseEntity<String> accessTokenResponse = googleOAuthHandler.requestAccessToken(code);
                // Deserialize From JSON to Object
                GoogleOAuthTokenDto googleOAuthTokenDto = googleOAuthHandler.getAccessToken(accessTokenResponse);

                // 액세스 토큰을 다시 구글 서버로 보내서 구글에 저장된 사용자 정보가 담긴 응답 객체를 받아온다.
                ResponseEntity<String> userInfoResponse = googleOAuthHandler.requestUserInfo(googleOAuthTokenDto);

                // Error handling
                if (userInfoResponse == null) {
                    throw new CustomAuthException(ErrorCode.SERVER_ERROR);
                }
                // Deserialize From JSON to Object
                GoogleOAuthUserDto googleOAuthUserDto = googleOAuthHandler.getUserInfoFromJson(userInfoResponse);
                log.info(">> 요청이 들어온 사용자 정보 :: provider=google, user e-mail={}", googleOAuthUserDto.getEmail());

                // 로그인, 회원가입 검증 로직
                List<User> userInDb = userRepository.findUserByEmailAndNameAndProvider(googleOAuthUserDto.getEmail(),
                        googleOAuthUserDto.getName(), googleOAuthUserDto.getProvider());

                if (userInDb.size() > 0) {
                    // 이미 가입된 유저이므로, 여기에서 access token, refresh token 을 만들어서 보낸다.


                } else {
                    // 신규 유저이므로, 회원가입 처리를 하도록 해야함

                    // Wrap user data from Jwt
                    String jwtInfo = generateUserDataJwt(googleOAuthUserDto);

                    return OAuthUserResponseDto.builder()
                            .status(HttpStatus.OK)
                            .message("요청이 정상적으로 처리 되었습니다. 회원가입 처리를 마저 진행해 주세요.")
                            .data(jwtInfo)
                            .build();
                }
            }
            case KAKAO: {
                // 카카오로 인가 코드를 보내 토큰을 받아온다.
                ResponseEntity<String> accessTokenResponse = kaKaoOauthHandler.requestAccessToken(code);
                // Deserialize From JSON to Object
                KaKaoOAuthTokenDto kaKaoOauthTokenDto = kaKaoOauthHandler.getAccessToken(accessTokenResponse);

                // 토큰 유효성 검증. 토큰으로 사용자 조회. 서비스 회원 정보 또는 가입 처리가 가능함.
                ResponseEntity<String> userInfoResponse = kaKaoOauthHandler.requestUserInfo(kaKaoOauthTokenDto);

                // Error handling
                if (userInfoResponse == null) {
                    throw new CustomAuthException(ErrorCode.SERVER_ERROR);
                }
                // Deserialize From JSON to Object
                KaKaoOAuthUserDto kaKaoOAuthUserDto = kaKaoOauthHandler.getUserInfoFromJson(userInfoResponse);
                log.info(">> 요청이 들어온 사용자 정보 :: provider=kakao, user e-mail={}", kaKaoOAuthUserDto.getKakaoAccount().getEmail());

                // 기존에 사이트에 가입된 유저인지 검증 필요
                List<User> userInDb = userRepository.findUserByEmailAndNameAndProvider(kaKaoOAuthUserDto.getEmail(),
                        kaKaoOAuthUserDto.getName(), kaKaoOAuthUserDto.getProvider());

                if (userInDb.size() > 0) {
                    throw new CustomAuthException(ErrorCode.USER_DUPLICATED);
                }

                // Wrap user data from Jwt
                String jwtInfo = generateUserDataJwt(kaKaoOAuthUserDto);

                return OAuthUserResponseDto.builder()
                        .status(HttpStatus.OK)
                        .message("요청이 정상적으로 처리 되었습니다.")
                        .data(jwtInfo)
                        .build();
            }
            default: {
                log.error(">> oAuthLogin error");
                throw new IllegalArgumentException("알 수 없는 소셜 로그인 형식입니다.");
            }
        }
    }

    private String generateUserDataJwt(OAuthUserDto oAuthUserDto) {
        Key key = getJwtSecretKey();  // use JwtUtils in common
        Date now = new Date();
        long expireTime = Duration.ofMinutes(120).toMillis();    // 만료시간 120분

        // Set header
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS512");

        String id;
        if (oAuthUserDto instanceof GoogleOAuthUserDto) {
            id = "google_" + oAuthUserDto.getId();
        } else {
            id = "kakao_" + oAuthUserDto.getId();
        }

        // Set payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("email", oAuthUserDto.getEmail());
        payload.put("name", oAuthUserDto.getName());
        payload.put("provider", oAuthUserDto.getProvider());

        return Jwts.builder()
                .setHeader(header)
                .setClaims(payload) // token 에서 사용할 정보의 조각들
                .setSubject("users")    // token 용도
                .setIssuedAt(now)   // token 발급 시간
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

}
