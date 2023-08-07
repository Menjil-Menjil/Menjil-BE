package seoultech.capstone.menjil.domain.auth.api;

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
import seoultech.capstone.menjil.domain.auth.dto.response.SignInResponseDto;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    /*
     * @WebMvcTest 에서는, db 조회는 힘들 것으로 생각하였으나,
     * Mockito.when().thenReturn() 을 통해 예상되는 동작을 미리 지정해준 뒤,
     * 그에 해당하는 응답값이 반환되는지 검증할 수 있다.
     */

    /**
     * checkSignupIsAvailable()
     */
    @Test
    @DisplayName("회원가입 하기 전에, 먼저 기존에 가입된 사용자인지 확인한다. " +
            "기존에 가입되어있는 사용자가 아니라면, SuccessCode.SIGNUP_AVAILABLE 리턴 ")
    void checkSignupIsAvailable() throws Exception {
        String email = "Junit-test@gmail.com";
        String provider = "google";

        Mockito.when(authService.checkUserExistsInDb(email, provider)).thenReturn(200);

        mvc.perform(get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.SIGNUP_AVAILABLE.getMessage())))
                .andDo(print());

        verify(authService).checkUserExistsInDb(email, provider);
    }

    @Test
    @DisplayName("회원가입 하기 전에, 먼저 기존에 가입된 사용자인지 확인한다. " +
            "기존에 가입되어있는 사용자이면, ErrorCode.USER_DUPLICATED 리턴 ")
    void checkSignupIsAvailable_user_already_existed() throws Exception {
        String email = "Junit-test@gmail.com";
        String provider = "google";

        Mockito.when(authService.checkUserExistsInDb(email, provider)).thenReturn(409);

        mvc.perform(get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.USER_DUPLICATED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.USER_DUPLICATED.getMessage())))
                .andDo(print());

        verify(authService).checkUserExistsInDb(email, provider);
    }

    /**
     * checkNicknameDuplicate()
     */
    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는경우 SuccessCode.NICKNAME_AVAILABLE 리턴")
    void checkNicknameDuplicate() throws Exception {
        String nickname = "test33AA가나마";
        int httpOkValue = HttpStatus.OK.value();

        Mockito.when(authService.checkNicknameDuplication(nickname)).thenReturn(httpOkValue);

        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.NICKNAME_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.NICKNAME_AVAILABLE.getMessage())))
                .andDo(print());

        verify(authService).checkNicknameDuplication(nickname);
    }

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException 을 발생시킨다")
    void checkNicknameDuplicate_nickname_is_blank() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_CONTAINS_BLANK.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.NICKNAME_CONTAINS_BLANK.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException 을 발생시킨다.")
    void checkNicknameDuplicate_nickname_has_special_char() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "*ea3sf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 닉네임이 db에 이미 존재하는 경우 ErrorCode.NICKNAME_DUPLICATED 리턴")
    void checkNicknameDuplicate_nickname_already_existed() throws Exception {
        String nickname = "NicknameExistsInDB";
        int httpConflictValue = HttpStatus.CONFLICT.value();

        Mockito.when(authService.checkNicknameDuplication(nickname)).thenReturn(httpConflictValue);

        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_DUPLICATED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_DUPLICATED.getMessage())))
                .andDo(print());

        verify(authService).checkNicknameDuplication(nickname);
    }

    /**
     * 회원가입 요청 검증
     */
    @Test
    @DisplayName("회원가입 요청이 정상적으로 된 경우 SuccessCode.SIGNUP_SUCCESS 리턴")
    void signUp() throws Exception {
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "hi", 1999, 3, "서울시립대", 3);
        String content = gson.toJson(signUpReqDto);

        int httpCreatedValue = HttpStatus.CREATED.value();
        Mockito.when(authService.signUp(signUpReqDto)).thenReturn(httpCreatedValue);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.SIGNUP_SUCCESS.getMessage())))
                .andDo(print());

        verify(authService).signUp(signUpReqDto);
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 공백이 포함된 경우 CustomException 발생")
    void signUp_nickname_contains_blank() throws Exception {
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
    void signUp_nickname_contains_character() throws Exception {
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
    void signUp_nickname_is_Null() throws Exception {
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
    void signUp_Null_is_morethan_two() throws Exception {
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
    void signUp_if_score_is_morethan_4() throws Exception {
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
    @DisplayName("로그인 시 google, kakao 외에 다른 플랫폼이 온 경우 ErrorCode.PROVIDER_NOT_ALLOWED 리턴")
    void signIn_provider_type_mismatch() throws Exception {
        // given
        String typeMismatch = "naver";
        SignInRequestDto requestDto = new SignInRequestDto("k337kk@kakao.com", typeMismatch);
        String content = gson.toJson(requestDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.PROVIDER_NOT_ALLOWED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.PROVIDER_NOT_ALLOWED.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("kakao 로그인이 잘 된경우, SuccessCode.TOKEN_CREATED 리턴")
    void signIn_kakao() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto("k337kk@kakao.com", "kakao");
        String content = gson.toJson(requestDto);

        SignInResponseDto signInResponseDto = SignInResponseDto.builder()
                .accessToken("test_access_token")
                .refreshToken("test_refresh_token")
                .build();

        Mockito.when(authService.signIn(requestDto.getEmail(), requestDto.getProvider())).thenReturn(signInResponseDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code",
                        is(SuccessCode.TOKEN_CREATED.getCode())))
                .andExpect(jsonPath("$.message",
                        is(SuccessCode.TOKEN_CREATED.getMessage())))
                .andExpect(jsonPath("$.data.accessToken",
                        is(signInResponseDto.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken",
                        is(signInResponseDto.getRefreshToken())))
                .andDo(print());

        verify(authService, times(1)).signIn(requestDto.getEmail(), requestDto.getProvider());
    }

    @Test
    @DisplayName("google 로그인이 잘 된경우, SuccessCode.TOKEN_CREATED 리턴")
    void signIn_google() throws Exception {
        // given
        SignInRequestDto requestDto = new SignInRequestDto("testUser@google.com", "google");
        String content = gson.toJson(requestDto);

        SignInResponseDto signInResponseDto = SignInResponseDto.builder()
                .accessToken("test_access_token")
                .refreshToken("test_refresh_token")
                .build();

        Mockito.when(authService.signIn(requestDto.getEmail(), requestDto.getProvider())).thenReturn(signInResponseDto);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code",
                        is(SuccessCode.TOKEN_CREATED.getCode())))
                .andExpect(jsonPath("$.message",
                        is(SuccessCode.TOKEN_CREATED.getMessage())))
                .andExpect(jsonPath("$.data.accessToken",
                        is(signInResponseDto.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken",
                        is(signInResponseDto.getRefreshToken())))
                .andDo(print());

        verify(authService, times(1)).signIn(requestDto.getEmail(), requestDto.getProvider());
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