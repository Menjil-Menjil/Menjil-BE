package seoultech.capstone.menjil.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.TokenTestController;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFilterTest {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret.token}")
    private String tokenKey;

    private final String TEST_USER_ID = "kakao_33441122";
    private final String REQUEST_URL = "/api/user/token-test";
    private String REFRESH_TOKEN_IN_DB;

    @BeforeEach
    public void setup() {
        mockMvc = standaloneSetup(new TokenTestController())
                .addFilters(new JwtAuthenticationFilter(
                        new JwtTokenProvider(tokenKey, userRepository, tokenRepository), objectMapper))
                .build();

        // save user in db
        User testUser = createUser(TEST_USER_ID, "filterTest@kakao.com", "kakao",
                "TestUser33");
        User dbUser = userRepository.save(testUser);    // DataIntegrityViolationException 방지를 위해, dbUser 로 값을 받는다.

        // save refresh token in db
        REFRESH_TOKEN_IN_DB = jwtTokenProvider.generateRefreshToken(TEST_USER_ID, LocalDateTime.now());
        LocalDateTime currentDateTime = LocalDateTime.now();
        Timestamp expiryDate = Timestamp.valueOf(currentDateTime.plusDays(14));

        RefreshToken refreshToken = RefreshToken.builder()
                .id(null)
                .userId(dbUser)
                .token(REFRESH_TOKEN_IN_DB)
                .expiryDate(expiryDate)
                .build();
        tokenRepository.save(refreshToken);
    }

    @Test
    @DisplayName("Case 1: 요청이 들어올 때, header 에 Authentication 정보가 없는 경우 403 오류 리턴")
    void authentication_is_none() throws Exception {
        mockMvc.perform(post(REQUEST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8")) // Verify the encoding
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message",
                        is("Header의 Authorization 값이 존재하지 않거나, 혹은 Authorization에서 Bearer 타입이 존재하지 않습니다")))
                .andExpect(jsonPath("$.data", is("None")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 1: 요청이 들어올 때, header 의 Authentication 타입이 Bearer 가 아닌 경우 403 오류 리턴")
    void authentication_type_is_not_valid() throws Exception {
        String type = "hello-Bearer";
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", type + " " + token1 + " " + token2 + " " + token3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8")) // Verify the encoding
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message",
                        is("Header의 Authorization 값이 존재하지 않거나, 혹은 Authorization에서 Bearer 타입이 존재하지 않습니다")))
                .andExpect(jsonPath("$.data", is("None")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 2: 요청이 들어올 때, header의 Authentication 값이 3개 이상인 경우 403 오류 리턴")
    void authentication_header_has_moreThan_two_values() throws Exception {
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", "Bearer " + token1 + " " + token2 + " " + token3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8")) // Verify the encoding
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message",
                        is("Authorization의 Bearer 값이 잘못되었습니다")))
                .andExpect(jsonPath("$.data", is("None")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 3: 요청이 들어올 때, Access Token 이 올바른 경우 TokenTestController 의 응답이 리턴")
    void accessToken_is_reliable() throws Exception {
        String validAccessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, LocalDateTime.now());

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message",
                        is("정상적으로 요청이 들어왔습니다")))
                .andExpect(jsonPath("$.data", is("정상 요청")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 3: 요청이 들어올 때, Access Token 이 올바르지 않은 경우 403 오류 리턴")
    void accessToken_is_not_reliable() throws Exception {
        String userIdNotInDb = "user_3373284718294319";
        String nonValidAccessToken = jwtTokenProvider.generateAccessToken(userIdNotInDb, LocalDateTime.now());

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", "Bearer " + nonValidAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.FORBIDDEN.value())))
                .andExpect(jsonPath("$.message",
                        is("Access Token 값이 유효하지 않습니다")))
                .andExpect(jsonPath("$.data", is("Access_Token_is_not_valid")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 4: Refresh Token 이 유효한 경우 Access Token 재발급")
    void AccessToken_reIssuance_when_refreshToken_is_reliable() throws Exception {
        String userIdNotInDb = "user_3373284718294319";
        String nonValidAccessToken = jwtTokenProvider.generateAccessToken(userIdNotInDb, LocalDateTime.now());

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", "Bearer " + nonValidAccessToken + " " + REFRESH_TOKEN_IN_DB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.message",
                        is("Access Token이 재발급 되었습니다")))
                .andDo(print());
    }

    @Test
    @DisplayName("Case 4: Refresh Token 이 유효하지 않은 경우(db에 값이 없는 경우) 403 오류 리턴")
    void refreshToken_is_not_reliable() throws Exception {
        String userIdNotInDb = "user_3373284718294319";
        String nonValidAccessToken = jwtTokenProvider.generateAccessToken(userIdNotInDb, LocalDateTime.now());
        String nonValidRefreshToken = jwtTokenProvider.generateRefreshToken(userIdNotInDb, LocalDateTime.now());

        mockMvc.perform(post(REQUEST_URL)
                        .header("Authorization", "Bearer " + nonValidAccessToken + " " + nonValidRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().encoding("UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.FORBIDDEN.value())))
                .andExpect(jsonPath("$.message",
                        is("Refresh Token 값이 유효하지 않습니다. 재로그인 해주세요")))
                .andExpect(jsonPath("$.data", is("Refresh_Token_is_not_valid")))
                .andDo(print());
    }

    private User createUser(String id, String email, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("고려대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .build();
    }
}