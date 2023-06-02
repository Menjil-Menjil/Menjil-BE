package seoultech.capstone.menjil.domain.auth.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.auth.application.OAuthService;
import seoultech.capstone.menjil.domain.auth.domain.SocialLoginType;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/auth")
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * 사용자로부터 SNS 로그인 요청을 Social Login Type 을 받아 처리
     *
     * @param socialLoginType; GOOGLE, KAKAO
     */
    @GetMapping(value = "/{socialLoginType}")
    @ResponseBody
    public void socialLoginType(@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {
        log.info(">> 사용자로부터 SNS 로그인 요청을 받음 :: {} Social Login", socialLoginType);
        oAuthService.requestRedirectURL(socialLoginType);
    }

    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     *
     * @param socialLoginType; GOOGLE, KAKAO
     * @param code;            API Server 로부터 넘어오는 code
     * @return OAuthUserDtoRes
     */
    @GetMapping(value = "/{socialLoginType}/callback")
    @ResponseBody
    public Object callback(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
            @RequestParam(name = "code") String code) throws JsonProcessingException {
        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        return oAuthService.oAuthLogin(socialLoginType, code);
    }
}
