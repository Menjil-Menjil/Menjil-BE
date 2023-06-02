package seoultech.capstone.menjil.domain.user.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.domain.UserRole;
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@TestPropertySource(locations = "classpath:application-jwt.properties")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    private String expiredUserJwtData = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJwc" +
            "m92aWRlciI6Imtha2FvIiwibmFtZSI6Iuq5gOuvvOykgCIsImlkIjoia2FrYW9fMjc3Nzg0" +
            "NzIzNSIsImVtYWlsIjoibWpqdW4zM0BrYWthby5jb20iLCJzdWIiOiJ1c2VycyIsImlhdCI6" +
            "MTY4MzYxNTUzNSwiZXhwIjoxNjgzNjE1NTM3fQ.EuO2-vjphB5vNmhNZ75xIi9-24FLBcoOM" +
            "yk3UaC0zFr9wHyfLepVuwf4jMYi7BmU07vDZpJpvlyCUYZGolf34w";

    private String jwtDataA;    // 정상적인 토큰 생성

    @Autowired
    private Environment env;

    @BeforeEach
    public void initGenerateJwtData() {
        // import application-jwt.properties
        String jwtSecret = env.getProperty("jwt.secret");

        // encode
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey JWT_SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);

        Date now = new Date();
        long expireTime = Duration.ofDays(360).toMillis();    // 만료날짜 360일 이후.

        // Set header
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS512");

        // Set payload A
        Map<String, Object> payloadA = new HashMap<>();
        payloadA.put("id", "test_A");
        payloadA.put("email", "userControllerTestA@gmail.com");
        payloadA.put("name", "testJwtUserA");
        payloadA.put("provider", "google");
        jwtDataA = Jwts.builder()
                .setHeader(header)
                .setClaims(payloadA)
                .setSubject("UserControllerTest")
                .setIssuedAt(now)   // token 발급 시간
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(JWT_SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException을 발생시킨다")
    public void NicknameIsBlank() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException을 발생시킨다")
    public void NicknameHasSpecialChar() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "*ea3sf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는 경우 정상적인 응답이 출력된다")
    public void NickNameCorrect() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "test33AA가나마"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; Jwt 토큰이 만료된 경우(@Expired) CustomException이 발생한다")
    public void jwtExpiredTest() throws Exception {
        UserRequestDto jwtExpiredDto = createUserRequestDto(expiredUserJwtData, "testA", 1999, 3, "서울과학기술대학교", 3);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(jwtExpiredDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. JWT 토큰이 만료되었습니다. 처음부터 다시 진행해 주세요 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; 닉네임에 공백이 포함된 경우 CustomException이 발생한다")
    public void nicknameHasBlankInSingUp() throws Exception {
        UserRequestDto userRequestDto = createUserRequestDto(jwtDataA, "test   A", 1999, 3, "서울과학기술대학교", 3);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; 닉네임에 특수문자가 포함된 경우 CustomException이 발생한다")
    public void nicknameHasCharacterInSingUp() throws Exception {
        UserRequestDto userRequestDto = createUserRequestDto(jwtDataA, "test#A", 1999, 3, "서울과학기술대학교", 3);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; @NotBlank 검증 ")
    public void validateNotBlankAnnotation() throws Exception {
        UserRequestDto userRequestDto = createUserRequestDto(jwtDataA, null, 1999, 3, "서울과학기술대학교", 3);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; @NotBlank 여러 개 검증")
    public void validateSeveralNotBlankAnnotation() throws Exception {
        UserRequestDto userRequestDto = createUserRequestDto(jwtDataA, null, 1999, 3, null, 3);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. must not be blank 2. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("가입 요청; @Max 검증")
    public void validateMaxSignUp() throws Exception {
        UserRequestDto userRequestDto = createUserRequestDto(jwtDataA, "testA", 1999, 3, "서울과학기술대학교", 6);

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(userRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 학점은 4보다 클 수 없습니다 ")))
                .andDo(print());
    }


    static String jsonToString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException error) {
            System.out.println("error = " + error);
            return "fail";
        }
    }

    private UserRequestDto createUserRequestDto(String data, String nickname,
                                                Integer birthYear, Integer birthMonth,
                                                String school, Integer score) {
        return UserRequestDto.builder()
                .data(data).nickname(nickname)
                .role(UserRole.MENTEE)
                .birthYear(birthYear).birthMonth(birthMonth)
                .school(school)
                .score(score).scoreRange("중반")
                .graduateDate(2021).major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null).certificate(null).awards(null).activity(null)
                .build();
    }

}