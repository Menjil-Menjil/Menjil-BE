package seoultech.capstone.menjil.docs.auth;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import seoultech.capstone.menjil.docs.RestDocsSupport;
import seoultech.capstone.menjil.domain.auth.api.AuthController;
import seoultech.capstone.menjil.domain.auth.api.dto.request.SignInRequest;
import seoultech.capstone.menjil.domain.auth.api.dto.request.SignUpRequest;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignInServiceRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.response.SignInResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
public class AuthControllerDocsTest extends RestDocsSupport {

    @MockBean
    private AuthService authService;

    @Autowired
    private Gson gson;

    @Test
    @DisplayName("case 1: 사용자가 존재하지 않는 경우")
    void checkSignupIsAvailable() throws Exception {
        // given
        String email = "testUser1@gmail.com";
        String provider = "google";

        // when
        Mockito.when(authService.findUserInDb(email, provider))
                .thenReturn(SuccessIntValue.SUCCESS.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_AVAILABLE.getCode())))
                .andDo(print())
                .andDo(document("api/auth/signup-is-available/true",
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
    }

    @Test
    @DisplayName("case 2: 사용자가 이미 존재하는 경우, 회원가입이 불가능하다")
    void checkSignupIsAvailable_user_already_existed() throws Exception {
        // given
        String email = "testUser2@gmail.com";
        String provider = "google";

        // when
        Mockito.when(authService.findUserInDb(email, provider)).thenReturn(ErrorIntValue.USER_ALREADY_EXISTED.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/signup")
                        .queryParam("email", email)
                        .queryParam("provider", provider))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_ALREADY_EXISTED.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/auth/signup-is-available/false",
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
    }

    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는 경우 정상 로직 수행")
    void isNicknameAvailable() throws Exception {
        // given
        String nickname = "test33AA";

        // when
        Mockito.when(authService.findNicknameInDb(nickname)).thenReturn(SuccessIntValue.SUCCESS.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.NICKNAME_AVAILABLE.getCode())))
                .andDo(print())
                .andDo(document("api/auth/check-nickname/true",
                        requestParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException 을 발생시킨다")
    void isNicknameAvailable_nickname_is_blank() throws Exception {
        // given
        String nickname = "  ";

        // when // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/auth/check-nickname/false-blank",
                        requestParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException 을 발생시킨다.")
    void isNicknameAvailable_nickname_has_special_char() throws Exception {
        String nickname = "easd##!@@@@asdf";

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.NICKNAME_FORMAT_IS_WRONG.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/auth/check-nickname/false-special-character",
                        requestParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 검증; 닉네임이 db에 이미 존재하는 경우 ErrorCode.NICKNAME_DUPLICATED 리턴")
    void isNicknameAvailable_nickname_already_existed() throws Exception {
        String nickname = "NicknameExistsInDB";

        Mockito.when(authService.findNicknameInDb(nickname)).thenReturn(ErrorIntValue.USER_ALREADY_EXISTED.getValue());

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/auth/check-nickname")
                        .queryParam("nickname", nickname))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_ALREADY_EXISTED.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/auth/check-nickname/false-conflict",
                        requestParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 요청이 정상적으로 된 경우 SuccessCode.SIGNUP_SUCCESS 리턴")
    void signUp() throws Exception {
        // given
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user11", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        // when
        Mockito.doNothing().when(authService).signUp(signUpReqDto.toServiceRequest());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.SIGNUP_SUCCESS.getCode())))
                .andDo(print())
                .andDo(document("api/auth/signup/true",
                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.STRING)
                                        .description("provider + '_' + random int(10자리). 무작위로 생성. e.g) google_1122334455"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("닉네임"),
                                fieldWithPath("birthYear").type(JsonFieldType.NUMBER)
                                        .description("생년. YYYY"),
                                fieldWithPath("birthMonth").type(JsonFieldType.NUMBER)
                                        .description("생월. MM"),
                                fieldWithPath("school").type(JsonFieldType.STRING)
                                        .description("대학교"),
                                fieldWithPath("score").type(JsonFieldType.NUMBER)
                                        .description("학점: 0, 1, 2, 3, 4"),
                                fieldWithPath("scoreRange").type(JsonFieldType.STRING)
                                        .description("초반, 중반, 후반"),
                                fieldWithPath("graduateDate").type(JsonFieldType.NUMBER)
                                        .description("졸업년도. YYYY"),
                                fieldWithPath("graduateMonth").type(JsonFieldType.NUMBER)
                                        .description("졸업월. MM"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("본전공"),
                                fieldWithPath("subMajor").type(JsonFieldType.STRING)
                                        .description("복수전공. null 값을 허용합니다"),
                                fieldWithPath("minor").type(JsonFieldType.STRING)
                                        .description("부전공. null 값을 허용합니다"),
                                fieldWithPath("company").type(JsonFieldType.STRING)
                                        .description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                fieldWithPath("companyYear").type(JsonFieldType.NUMBER)
                                        .description("첫 입사 년도. 정보가 없는 경우 0값을 보내주세요"),
                                fieldWithPath("field").type(JsonFieldType.STRING)
                                        .description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                fieldWithPath("techStack").type(JsonFieldType.STRING)
                                        .description("기술 스택. 여러 개면 ','로 구분"),
                                fieldWithPath("career").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("certificate").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("awards").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("activity").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(STRING).description("응답 데이터: 가입이 처리된 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 공백이 포함된 경우 CustomException 발생")
    void signUp_nickname_contains_blank() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user One", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print())
                .andDo(document("api/auth/signup/false-blank",
                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.STRING)
                                        .description("provider + '_' + random int(10자리). 무작위로 생성. e.g) google_1122334455"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("닉네임"),
                                fieldWithPath("birthYear").type(JsonFieldType.NUMBER)
                                        .description("생년. YYYY"),
                                fieldWithPath("birthMonth").type(JsonFieldType.NUMBER)
                                        .description("생월. MM"),
                                fieldWithPath("school").type(JsonFieldType.STRING)
                                        .description("대학교"),
                                fieldWithPath("score").type(JsonFieldType.NUMBER)
                                        .description("학점: 0, 1, 2, 3, 4"),
                                fieldWithPath("scoreRange").type(JsonFieldType.STRING)
                                        .description("초반, 중반, 후반"),
                                fieldWithPath("graduateDate").type(JsonFieldType.NUMBER)
                                        .description("졸업년도. YYYY"),
                                fieldWithPath("graduateMonth").type(JsonFieldType.NUMBER)
                                        .description("졸업월. MM"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("본전공"),
                                fieldWithPath("subMajor").type(JsonFieldType.STRING)
                                        .description("복수전공. null 값을 허용합니다"),
                                fieldWithPath("minor").type(JsonFieldType.STRING)
                                        .description("부전공. null 값을 허용합니다"),
                                fieldWithPath("company").type(JsonFieldType.STRING)
                                        .description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                fieldWithPath("companyYear").type(JsonFieldType.NUMBER)
                                        .description("첫 입사 년도. 정보가 없는 경우 0값을 보내주세요"),
                                fieldWithPath("field").type(JsonFieldType.STRING)
                                        .description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                fieldWithPath("techStack").type(JsonFieldType.STRING)
                                        .description("기술 스택. 여러 개면 ','로 구분"),
                                fieldWithPath("career").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("certificate").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("awards").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("activity").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 특수문자가 포함된 경우 CustomException 발생")
    void signUp_nickname_contains_character() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                "user@@!!!One", 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print())
                .andDo(document("api/auth/signup/false-special-character",
                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.STRING)
                                        .description("provider + '_' + random int(10자리). 무작위로 생성. e.g) google_1122334455"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("닉네임"),
                                fieldWithPath("birthYear").type(JsonFieldType.NUMBER)
                                        .description("생년. YYYY"),
                                fieldWithPath("birthMonth").type(JsonFieldType.NUMBER)
                                        .description("생월. MM"),
                                fieldWithPath("school").type(JsonFieldType.STRING)
                                        .description("대학교"),
                                fieldWithPath("score").type(JsonFieldType.NUMBER)
                                        .description("학점: 0, 1, 2, 3, 4"),
                                fieldWithPath("scoreRange").type(JsonFieldType.STRING)
                                        .description("초반, 중반, 후반"),
                                fieldWithPath("graduateDate").type(JsonFieldType.NUMBER)
                                        .description("졸업년도. YYYY"),
                                fieldWithPath("graduateMonth").type(JsonFieldType.NUMBER)
                                        .description("졸업월. MM"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("본전공"),
                                fieldWithPath("subMajor").type(JsonFieldType.STRING)
                                        .description("복수전공. null 값을 허용합니다"),
                                fieldWithPath("minor").type(JsonFieldType.STRING)
                                        .description("부전공. null 값을 허용합니다"),
                                fieldWithPath("company").type(JsonFieldType.STRING)
                                        .description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                fieldWithPath("companyYear").type(JsonFieldType.NUMBER)
                                        .description("첫 입사 년도. 정보가 없는 경우 0값을 보내주세요"),
                                fieldWithPath("field").type(JsonFieldType.STRING)
                                        .description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                fieldWithPath("techStack").type(JsonFieldType.STRING)
                                        .description("기술 스택. 여러 개면 ','로 구분"),
                                fieldWithPath("career").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("certificate").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("awards").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("activity").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 값이 null 인 경우 @NotBlank")
    void signUp_nickname_is_Null() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_1122334455", "test@google.com", "google",
                null, 1999, 3, "서울과학기술대학교", 3);
        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print())
                .andDo(document("api/auth/signup/false-null",
                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.STRING)
                                        .description("provider + '_' + random int(10자리). 무작위로 생성. e.g) google_1122334455"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼"),
                                // NULL을 넣었으므로, 이 부분 주석 처리
//                                fieldWithPath("nickname").type(JsonFieldType.NULL)
//                                        .description("닉네임"),
                                fieldWithPath("birthYear").type(JsonFieldType.NUMBER)
                                        .description("생년. YYYY"),
                                fieldWithPath("birthMonth").type(JsonFieldType.NUMBER)
                                        .description("생월. MM"),
                                fieldWithPath("school").type(JsonFieldType.STRING)
                                        .description("대학교"),
                                fieldWithPath("score").type(JsonFieldType.NUMBER)
                                        .description("학점: 0, 1, 2, 3, 4"),
                                fieldWithPath("scoreRange").type(JsonFieldType.STRING)
                                        .description("초반, 중반, 후반"),
                                fieldWithPath("graduateDate").type(JsonFieldType.NUMBER)
                                        .description("졸업년도. YYYY"),
                                fieldWithPath("graduateMonth").type(JsonFieldType.NUMBER)
                                        .description("졸업월. MM"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("본전공"),
                                fieldWithPath("subMajor").type(JsonFieldType.STRING)
                                        .description("복수전공. null 값을 허용합니다"),
                                fieldWithPath("minor").type(JsonFieldType.STRING)
                                        .description("부전공. null 값을 허용합니다"),
                                fieldWithPath("company").type(JsonFieldType.STRING)
                                        .description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                fieldWithPath("companyYear").type(JsonFieldType.NUMBER)
                                        .description("첫 입사 년도. 정보가 없는 경우 0값을 보내주세요"),
                                fieldWithPath("field").type(JsonFieldType.STRING)
                                        .description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                fieldWithPath("techStack").type(JsonFieldType.STRING)
                                        .description("기술 스택. 여러 개면 ','로 구분"),
                                fieldWithPath("career").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("certificate").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("awards").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("activity").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 요청 시 @Max 검증: 학점이 4 이상인 경우")
    void signUp_if_score_is_more_than_4() throws Exception {
        SignUpRequest signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "hi", 1999, 3, "서울시립대", 6);

        String content = gson.toJson(signUpReqDto);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print())
                .andDo(document("api/auth/signup/false-score-max",
                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.STRING)
                                        .description("provider + '_' + random int(10자리). 무작위로 생성. e.g) google_1122334455"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("닉네임"),
                                fieldWithPath("birthYear").type(JsonFieldType.NUMBER)
                                        .description("생년. YYYY"),
                                fieldWithPath("birthMonth").type(JsonFieldType.NUMBER)
                                        .description("생월. MM"),
                                fieldWithPath("school").type(JsonFieldType.STRING)
                                        .description("대학교"),
                                fieldWithPath("score").type(JsonFieldType.NUMBER)
                                        .description("학점: 0, 1, 2, 3, 4"),
                                fieldWithPath("scoreRange").type(JsonFieldType.STRING)
                                        .description("초반, 중반, 후반"),
                                fieldWithPath("graduateDate").type(JsonFieldType.NUMBER)
                                        .description("졸업년도. YYYY"),
                                fieldWithPath("graduateMonth").type(JsonFieldType.NUMBER)
                                        .description("졸업월. MM"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("본전공"),
                                fieldWithPath("subMajor").type(JsonFieldType.STRING)
                                        .description("복수전공. null 값을 허용합니다"),
                                fieldWithPath("minor").type(JsonFieldType.STRING)
                                        .description("부전공. null 값을 허용합니다"),
                                fieldWithPath("company").type(JsonFieldType.STRING)
                                        .description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                fieldWithPath("companyYear").type(JsonFieldType.NUMBER)
                                        .description("첫 입사 년도. 정보가 없는 경우 0값을 보내주세요"),
                                fieldWithPath("field").type(JsonFieldType.STRING)
                                        .description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                fieldWithPath("techStack").type(JsonFieldType.STRING)
                                        .description("기술 스택. 여러 개면 ','로 구분"),
                                fieldWithPath("career").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("certificate").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("awards").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다"),
                                fieldWithPath("activity").type(JsonFieldType.STRING)
                                        .description("null 값을 허용합니다")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

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
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code",
                        is(ErrorCode.PROVIDER_NOT_ALLOWED.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/auth/signin/fail-provider",
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    /**
     * 로그인 성공 로직. 테스트 코드와는 달리, kakao 경우 1개만 작성
     */
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
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code",
                        is(SuccessCode.TOKEN_CREATED.getCode())))
                .andDo(print())
                .andDo(document("api/auth/signin/success",
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("provider").type(JsonFieldType.STRING)
                                        .description("이메일 플랫폼")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data.accessToken").description("Access token"),
                                fieldWithPath("data.refreshToken").description("Refresh token"),
                                fieldWithPath("data.nickname").description("사용자의 닉네임"),
                                fieldWithPath("data.school").description("사용자의 학교"),
                                fieldWithPath("data.major").description("사용자의 전공"),
                                fieldWithPath("data.imgUrl").description("사용자의 프로필 이미지 url")
                        )
                ));

    }


    private SignUpRequest createSignUpReqDto(String id, String email, String provider, String nickname,
                                             Integer birthYear, Integer birthMonth,
                                             String school, Integer score) {
        String major = "컴퓨터공학과";
        String subMajor = "경영학과";
        String minor = "인공지능응용학과";
        String company = "NAVER";
        Integer companyYear = 0;
        String field = "백엔드";
        String techStack = "AWS";
        return new SignUpRequest(id, email, provider, nickname,
                birthYear, birthMonth, school,
                score, "중반", 2021, 3, major, subMajor, minor, company, companyYear,
                field, techStack,"NAVER 2023.02~", "정보처리기사", "공모전 입상", "SOPT");
    }
}
