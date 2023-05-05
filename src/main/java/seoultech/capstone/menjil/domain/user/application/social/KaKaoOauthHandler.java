package seoultech.capstone.menjil.domain.user.application.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import seoultech.capstone.menjil.domain.user.dto.KaKaoOAuthTokenDto;
import seoultech.capstone.menjil.domain.user.dto.KaKaoOAuthUserDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KaKaoOauthHandler implements SocialOAuthHandler {

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_OAUTH_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String KAKAO_OAUTH_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_OAUTH_REDIRECT_URI;

    private static final String KAKAO_OAUTH_ENDPOINT_URL = "https://kauth.kakao.com/oauth/authorize";

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("scope", "profile_nickname account_email");
        params.put("response_type", "code");
        params.put("client_id", KAKAO_OAUTH_CLIENT_ID);
        params.put("redirect_uri", KAKAO_OAUTH_REDIRECT_URI);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return KAKAO_OAUTH_ENDPOINT_URL + "?" + parameterString;
    }

    @Override
    public ResponseEntity<String> requestAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", KAKAO_OAUTH_CLIENT_ID);
//        params.add("client_secret", KAKAO_OAUTH_CLIENT_SECRET);
        params.add("redirect_url", KAKAO_OAUTH_REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST, entity, String.class);

        return response;
    }

    /**
     * JSON String => Deserialize(역직렬화) => Java Object
     */
    @Override
    public KaKaoOAuthTokenDto getAccessToken(ResponseEntity<String> response) {
        System.out.println("response.getBody() = " + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        KaKaoOAuthTokenDto kaKaoOauthTokenDto = new KaKaoOAuthTokenDto();
        try {
            kaKaoOauthTokenDto = objectMapper.readValue(response.getBody(), KaKaoOAuthTokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("jsonProcessing Error, kaKaoOauthToken = {}", kaKaoOauthTokenDto.toString());
            e.printStackTrace();
        }
        return kaKaoOauthTokenDto;
    }

    public ResponseEntity<String> requestUserInfo(KaKaoOAuthTokenDto token) {

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/x-www-form-urlencoded");
        headers.set("Authorization", "Bearer " + token.getAccess_token());
        // body 정보는 따로 필요 없음.

        // 요청하기 위해 Header 를 HttpEntity 로 묶기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity(headers);

        // GET 요청
        ResponseEntity<String> response = restTemplate.
                exchange("https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET, entity, String.class);

        return response;
    }

    public KaKaoOAuthUserDto getUserInfoFromJson(ResponseEntity<String> userInfoRes) {
        System.out.println("getUserInfoFromJson.getBody() = " + userInfoRes.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        KaKaoOAuthUserDto kaKaoOAuthUserDto = new KaKaoOAuthUserDto();
        try {
            kaKaoOAuthUserDto = objectMapper.readValue(userInfoRes.getBody(), KaKaoOAuthUserDto.class);
        } catch (JsonProcessingException e) {
            log.error("jsonProcessing Error, kaKaoOAuthUser = {}", kaKaoOAuthUserDto.toString());
            e.printStackTrace();
        }
        return kaKaoOAuthUserDto;
    }

}
