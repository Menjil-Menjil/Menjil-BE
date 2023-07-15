package seoultech.capstone.menjil.domain.auth.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.auth.dto.request.SignInRequestDto;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequestDto;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Gson gson;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("회원가입 하기 전에, 먼저 기존에 가입된 사용자인지 확인한다. " +
            "하지만 db 조회까지는 여기서 힘드므로, 그냥 200 OK 응답만 검증")
    void checkSignupIsAvailable() throws Exception {
        String email = "Junit-test@gmail.com";
        String provider = "google";

        Mockito.when(authService.checkUserExistsInDb(email, provider)).thenReturn(200);

        mvc.perform(get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andDo(print());

        verify(authService).checkUserExistsInDb(email, provider);
    }

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException 을 발생시킨다")
    public void nicknameIsBlank() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException 을 발생시킨다.")
    public void nicknameHasSpecialChar() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "*ea3sf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는경우 정상적인 동작이 수행된다.")
    public void nicknameIsAvailable() throws Exception {
        String nickname = "test33AA가나마";
        int httpOkValue = HttpStatus.OK.value();

        Mockito.when(authService.checkNicknameDuplication(nickname)).thenReturn(httpOkValue);

        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("사용 가능한 닉네임입니다")))
                .andDo(print());

        verify(authService).checkNicknameDuplication(nickname);
    }

    /**
     * 회원가입 요청 검증
     */
    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 공백이 포함된 경우 CustomException 발생")
    void signUpNicknameHasBlank() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_2134", "test@kakao.com", "kakao",
                "가나 다라마", 1999, 3, "서울과기대", 3);

        String content = gson.toJson(signUpReqDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 특수문자가 포함된 경우 CustomException 발생")
    public void signUpNicknameHasCharacter() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "가나@다라마", 1999, 3, "서울과기대", 3);

        String content = gson.toJson(signUpReqDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 값이 null 인 경우 @NotBlank")
    public void signUpNicknameIsNull() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                null, 1999, 3, "서울과기대", 3);

        String content = gson.toJson(signUpReqDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 @NotBlank 여러 개 검증: null 값이 2개인 경우")
    public void singUpNullIsMoreThanTwo() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                null, 1999, 3, null, 3);

        String content = gson.toJson(signUpReqDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. must not be blank 2. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 @Max 검증: 학점이 4 이상인 경우")
    public void signUpValidateMax() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "hi", 1999, 3, "서울시립대", 6);

        String content = gson.toJson(signUpReqDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. 학점은 4보다 클 수 없습니다 ")))
                .andDo(print());
    }

    /**
     * 로그인(signIn) 검증
     */
    @Test
    @DisplayName("로그인 시 google, kakao 외에 다른 플랫폼이 온 경우 오류처리")
    void signInProvider() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto("k337kk@kakao.com", "naver");
        String content = gson.toJson(requestDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("가입 양식이 잘못되었습니다. 구글이나 카카오로 가입 요청을 해주세요")))
                .andDo(print());
    }

    private SignUpRequestDto createSignUpReqDto(String id, String email, String provider, String nickname,
                                                Integer birthYear, Integer birthMonth,
                                                String school, Integer score) {
        return new SignUpRequestDto(id, email, provider, nickname,
                UserRole.MENTEE, birthYear, birthMonth, school,
                score, "중반", 2021, 3, "경제학과", null, null,
                "Devops", "AWS", null, null, null, null);
    }

}