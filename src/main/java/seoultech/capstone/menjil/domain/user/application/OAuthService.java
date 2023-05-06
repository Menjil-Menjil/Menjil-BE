package seoultech.capstone.menjil.domain.user.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.user.application.handler.GoogleOAuthHandler;
import seoultech.capstone.menjil.domain.user.application.handler.KaKaoOauthHandler;
import seoultech.capstone.menjil.domain.user.domain.SocialLoginType;
import seoultech.capstone.menjil.domain.user.dto.GoogleOAuthTokenDto;
import seoultech.capstone.menjil.domain.user.dto.GoogleOAuthUserDto;
import seoultech.capstone.menjil.domain.user.dto.KaKaoOAuthTokenDto;
import seoultech.capstone.menjil.domain.user.dto.KaKaoOAuthUserDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {
    private final GoogleOAuthHandler googleOAuthHandler;
    private final KaKaoOauthHandler kaKaoOauthHandler;
    private final HttpServletResponse response;

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

    public String oAuthLogin(SocialLoginType socialLoginType, String code) throws JsonProcessingException {
        switch (socialLoginType) {
            case GOOGLE: {
                // 구글로 code 를 보내 액세스 토큰이 담긴 응답 객체를 받아온다.
                ResponseEntity<String> accessTokenResponse = googleOAuthHandler.requestAccessToken(code);
                // Deserialize From JSON to Object
                GoogleOAuthTokenDto googleOAuthTokenDto = googleOAuthHandler.getAccessToken(accessTokenResponse);

                // 액세스 토큰을 다시 구글로 보내서 구글에 저장된 사용자 정보가 담긴 응답 객체를 받아온다.
                ResponseEntity<String> userInfoResponse = googleOAuthHandler.requestUserInfo(googleOAuthTokenDto);
                // Deserialize From JSON to Object
                GoogleOAuthUserDto googleOAuthUserDto = googleOAuthHandler.getUserInfoFromJson(userInfoResponse);
                log.info(">> 요청이 들어온 사용자 정보 :: provider=google, user e-mail={}", googleOAuthUserDto.getEmail());
                return googleOAuthUserDto.toString();
            }
            case KAKAO: {
                // 카카오로 인가 코드를 보내 토큰을 받아온다.
                ResponseEntity<String> accessTokenResponse = kaKaoOauthHandler.requestAccessToken(code);
                // Deserialize From JSON to Object
                KaKaoOAuthTokenDto kaKaoOauthTokenDto = kaKaoOauthHandler.getAccessToken(accessTokenResponse);

                // 토큰 유효성 검증. 토큰으로 사용자 조회. 서비스 회원 정보 또는 가입 처리가 가능함.
                ResponseEntity<String> userInfoResponse = kaKaoOauthHandler.requestUserInfo(kaKaoOauthTokenDto);
                // Deserialize From JSON to Object
                KaKaoOAuthUserDto kaKaoOAuthUserDto = kaKaoOauthHandler.getUserInfoFromJson(userInfoResponse);
                log.info(">> 요청이 들어온 사용자 정보 :: provider=kakao, user e-mail={}", kaKaoOAuthUserDto.getKakaoAccount().getEmail());
                return kaKaoOAuthUserDto.toString();
            }
            default: {
                throw new IllegalArgumentException("알 수 없는 소셜 로그인 형식입니다.");
            }
        }
    }


}
