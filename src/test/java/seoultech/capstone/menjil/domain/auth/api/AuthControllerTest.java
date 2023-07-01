package seoultech.capstone.menjil.domain.auth.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequestDto;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("닉네임 검증; 공백이 들어오면 CustomException 을 발생시킨다")
    public void NicknameIsBlank() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자가 들어오면 CustomException 을 발생시킨다.")
    public void NicknameHasSpecialChar() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "*ea3sf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 공백과 특수문자가 없는경우 정상적인 동작이 수행된다.")
    public void NickNameCorrect() throws Exception {
        mvc.perform(get("/api/auth/check-nickname")
                        .queryParam("nickname", "test33AA가나마"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    /**
     * 아래부터 회원가입 검증
     */
    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 공백이 포함된 경우 CustomException 발생")
    void signUpNicknameHasBlank() throws Exception {
        // given
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_2134", "test@kakao.com", "kakao",
                "가나 다라마", 1999, 3, "서울과기대", 3);

        // when
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(signUpReqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 닉네임에 특수문자가 포함된 경우 CustomException 발생")
    public void signUpNicknameHasCharacter() throws Exception {
        // given
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "가나@다라마", 1999, 3, "서울과기대", 3);


        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(signUpReqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 닉네임은 공백이나 특수문자가 들어갈 수 없습니다 ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 닉네임 검증: 값이 null 인 경우 @NotBlank")
    public void signUpNicknameIsNull() throws Exception {
        // given
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                null, 1999, 3, "서울과기대", 3);


        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(signUpReqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 @NotBlank 여러 개 검증: null 값이 2개인 경우")
    public void singUpNullIsMoreThanTwo() throws Exception {
        // given
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                null, 1999, 3, null, 3);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(signUpReqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. must not be blank 2. must not be blank ")))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청 시 @Max 검증: 학점이 4 이상인 경우")
    public void signUpValidateMax() throws Exception {
        // given
        SignUpRequestDto signUpReqDto = createSignUpReqDto("google_213", "tes@google.com", "google",
                "hi", 1999, 3, "서울시립대", 6);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(signUpReqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. 학점은 4보다 클 수 없습니다 ")))
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

    static String jsonToString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException error) {
            System.out.println("error = " + error);
            return "fail";
        }
    }
}