package seoultech.capstone.menjil.domain.auth.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.api.dto.request.SignInRequest;
import seoultech.capstone.menjil.domain.auth.api.dto.request.SignUpRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignInServiceRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.response.SignInResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
@AutoConfigureRestDocs
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private AuthService authService;

    /**
     * checkSignupIsAvailable
     * 회원가입 하기 전에, 먼저 기존에 가입된 사용자인지 확인한다
     */
    @Test
    @DisplayName("case 1: 사용자가 존재하지 않는 경우, 회원가입이 가능하다")
    void checkSignupIsAvailable() throws Exception {
        // given
        String email = "testUser1@gmail.com";
        String provider = "google";

        // when
        Mockito.when(authService.findUserInDb(email, provider)).thenReturn(SuccessIntValue.SUCCESS.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.SIGNUP_AVAILABLE.getMessage())))
                .andDo(print());

        verify(authService).findUserInDb(email, provider);
    }

    @Test
    @DisplayName("case 2: 사용자가 이미 존재하는 경우, 회원가입이 불가능하다")
    void checkSignupIsAvailable_user_already_existed() throws Exception {
        // given
        String email = "testUser1@gmail.com";
        String provider = "google";

        BDDMockito.given(authService.findUserInDb(email, provider))
                .willReturn(ErrorIntValue.USER_ALREADY_EXISTED.getValue());

        // when // then
        mockMvc.perform(get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.USER_ALREADY_EXISTED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.USER_ALREADY_EXISTED.getMessage())))
                .andDo(print())
                .andDo(document("api/auth/signup-is-available/false",
                        preprocessRequest(prettyPrint()),   // show json pretty in asciidoc
                        preprocessResponse(prettyPrint()),  // show json pretty in asciidoc
                        requestParameters(
                                parameterWithName("email").description("사용자 이메일"),
                                parameterWithName("provider").description("이메일 플랫폼")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));

        verify(authService).findUserInDb(email, provider);
    }

    /**
     * isNicknameAvailable
     */
    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는 경우 정상 로직 수행")
    void isNicknameAvailable() throws Exception {
        // given
        String nickname = "test33AA";

        // when
        Mockito.when(authService.findNicknameInDb(nickname)).thenReturn(SuccessIntValue.SUCCESS.getValue());

        // then
        mockMvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.NICKNAME_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.NICKNAME_AVAILABLE.getMessage())))
                .andDo(print());

        verify(authService).findNicknameInDb(nickname);
    }

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException 을 발생시킨다")
    void isNicknameAvailable_nickname_is_blank() throws Exception {
        String nickname = "   ";

        mockMvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException 을 발생시킨다.")
    void isNicknameAvailable_nickname_has_special_char() throws Exception {
        String nickname = "easd##!@@@@asdf";

        mockMvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getMessage())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 닉네임이 db에 이미 존재하는 경우 ErrorCode.NICKNAME_DUPLICATED 리턴")
    void isNicknameAvailable_nickname_already_existed() throws Exception {
        String nickname = "NicknameExistsInDB";

        Mockito.when(authService.findNicknameInDb(nickname)).thenReturn(ErrorIntValue.USER_ALREADY_EXISTED.getValue());

        mockMvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_ALREADY_EXISTED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_ALREADY_EXISTED.getMessage())))
                .andDo(print());

        verify(authService).findNicknameInDb(nickname);
    }

    /**
     * signUp
     */
    @Test
    @DisplayName("회원가입 요청이 정상적으로 된 경우 SuccessCode.SIGNUP_SUCCESS 리턴")
    void signUp() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user11", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        // Return type of authService.signUp method is void
        Mockito.doNothing().when(authService).signUp(signUpReqDto.toServiceRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.SIGNUP_SUCCESS.getMessage())))
                .andDo(print());

        verify(authService).signUp(signUpReqDto.toServiceRequest());
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 공백이 포함된 경우 CustomException 발생")
    void signUp_nickname_contains_blank() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user One", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
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
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user@@!!!One", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
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
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                null, 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("1. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 @NotBlank 여러 개 검증: null 값이 2개인 경우")
    void signUp_Null_is_more_than_two() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                null, 1999, 3, null, 3);

        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
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
    void signUp_if_score_is_more_than_4() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "hi", 1999, 3, "서울시립대", 6);

        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
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
        String providerMisMatch = "naver";
        SignInRequest request = new SignInRequest("k337kk@kakao.com", providerMisMatch);
        String content = gson.toJson(request);

        // when
        Mockito.when(authService.signIn(Mockito.any(SignInServiceRequest.class)))
                .thenThrow(new CustomException(ErrorCode.PROVIDER_NOT_ALLOWED));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.PROVIDER_NOT_ALLOWED.getHttpStatus().value())))
                .andExpect(jsonPath("$.message",
                        is(ErrorCode.PROVIDER_NOT_ALLOWED.getMessage())))
                .andDo(print());

        verify(authService, times(1)).signIn(Mockito.any(SignInServiceRequest.class));
    }

    @Test
    @DisplayName("kakao 로그인이 잘 된경우, SuccessCode.TOKEN_CREATED 리턴")
    void signIn_kakao() throws Exception {
        // given
        SignInRequest request = new SignInRequest("k337kk@kakao.com", "kakao");
        String content = gson.toJson(request);

        SignInResponse signInResponse = SignInResponse.of("access_token", "refresh_token",
                "사용자1", "서울과학기술대학교", "컴퓨터공학과", "[img url]");

        // when
        Mockito.when(authService.signIn(Mockito.any(SignInServiceRequest.class))).thenReturn(signInResponse);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code",
                        is(SuccessCode.TOKEN_CREATED.getCode())))
                .andExpect(jsonPath("$.message",
                        is(SuccessCode.TOKEN_CREATED.getMessage())))
                .andExpect(jsonPath("$.data.accessToken",
                        is(signInResponse.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken",
                        is(signInResponse.getRefreshToken())))
                .andDo(print());

        verify(authService, times(1)).signIn(Mockito.any(SignInServiceRequest.class));
    }

    @Test
    @DisplayName("google 로그인이 잘 된경우, SuccessCode.TOKEN_CREATED 리턴")
    void signIn_google() throws Exception {
        // given
        SignInRequest request = new SignInRequest("testUser@google.com", "google");
        String content = gson.toJson(request);

        SignInResponse signInResponse = SignInResponse.of("access_token", "refresh_token",
                "사용자1", "서울과학기술대학교", "컴퓨터공학과", "[img url]");

        Mockito.when(authService.signIn(Mockito.any(SignInServiceRequest.class))).thenReturn(signInResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code",
                        is(SuccessCode.TOKEN_CREATED.getCode())))
                .andExpect(jsonPath("$.message",
                        is(SuccessCode.TOKEN_CREATED.getMessage())))
                .andExpect(jsonPath("$.data.accessToken",
                        is(signInResponse.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken",
                        is(signInResponse.getRefreshToken())))
                .andDo(print());

        verify(authService, times(1)).signIn(Mockito.any(SignInServiceRequest.class));
    }

    private SignUpRequest createSignUpReqDto(String id, String email, String provider, String nickname,
                                             Integer birthYear, Integer birthMonth,
                                             String school, Integer score) {
        String major = "컴퓨터공학과";
        String subMajor = null;
        String minor = null;
        String company = null;
        Integer companyYear = 3;
        String field = "백엔드";
        String techStack = "AWS";
        return new SignUpRequest(id, email, provider, nickname,
                birthYear, birthMonth, school,
                score, "중반", 2021, 3, major, subMajor, minor, company, companyYear,
                field, techStack,null, null, null, null);
    }

}