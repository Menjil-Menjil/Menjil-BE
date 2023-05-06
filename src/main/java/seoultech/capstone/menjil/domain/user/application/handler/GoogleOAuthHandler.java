package seoultech.capstone.menjil.domain.user.application.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import seoultech.capstone.menjil.domain.user.dto.GoogleOAuthTokenDto;
import seoultech.capstone.menjil.domain.user.dto.GoogleOAuthUserDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GoogleOAuthHandler implements SocialOAuthHandler {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_OAUTH_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_OAUTH_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String GOOGLE_OAUTH_REDIRECT_URI;

    private static final String GOOGLE_OAUTH_ENDPOINT_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_OAUTH_TOKEN_BASED_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_OAUTH_USERINFO_REQUEST_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GoogleOAuthHandler(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new ConcurrentHashMap<>();
//        params.put("scope", "profile");
        params.put("scope", "profile email");
        params.put("response_type", "code");
        params.put("client_id", GOOGLE_OAUTH_CLIENT_ID);
        params.put("redirect_uri", GOOGLE_OAUTH_REDIRECT_URI);
        params.put("access_type", "offline");   // for get refresh token

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return GOOGLE_OAUTH_ENDPOINT_URL + "?" + parameterString;
    }

    @Override
    public ResponseEntity<String> requestAccessToken(String code) {
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("code", code);
        params.put("client_id", GOOGLE_OAUTH_CLIENT_ID);
        params.put("client_secret", GOOGLE_OAUTH_CLIENT_SECRET);
        params.put("redirect_uri", GOOGLE_OAUTH_REDIRECT_URI);
        params.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(GOOGLE_OAUTH_TOKEN_BASED_URL, params, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }
        return null;
    }

    /**
     * JSON String => Deserialize(역직렬화) => Java Object
     */
    @Override
    public GoogleOAuthTokenDto getAccessToken(ResponseEntity<String> response) {
        GoogleOAuthTokenDto googleOAuthTokenDto = new GoogleOAuthTokenDto();
        try {
            googleOAuthTokenDto = objectMapper.readValue(response.getBody(), GoogleOAuthTokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("jsonProcessing Error, googleOAuthToken = {}", googleOAuthTokenDto.toString());
            e.printStackTrace();
        }
        return googleOAuthTokenDto;
    }

    public ResponseEntity<String> requestUserInfo(GoogleOAuthTokenDto token) {
        // header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        // body 정보는 따로 필요 없음.

        // 요청하기 위해 Header 를 HttpEntity 로 묶기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity(headers);

        // GET 요청
        ResponseEntity<String> response = restTemplate.
                exchange(GOOGLE_OAUTH_USERINFO_REQUEST_URL,
                        HttpMethod.GET, entity, String.class);

        return response;
    }

    @Override
    public GoogleOAuthUserDto getUserInfoFromJson(ResponseEntity<String> userInfoRes) {
        GoogleOAuthUserDto googleOAuthUserDto = new GoogleOAuthUserDto();
        try {
            googleOAuthUserDto = objectMapper.readValue(userInfoRes.getBody(), GoogleOAuthUserDto.class);
        } catch (JsonProcessingException e) {
            log.error("jsonProcessing Error, googleOAuthUser = {}", googleOAuthUserDto.toString());
            e.printStackTrace();
        }
        return googleOAuthUserDto;
    }
}
