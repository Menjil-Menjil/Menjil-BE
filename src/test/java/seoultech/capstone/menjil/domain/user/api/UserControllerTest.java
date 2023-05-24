package seoultech.capstone.menjil.domain.user.api;

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
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.domain.UserRole;
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
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

    @Test
    @DisplayName("닉네임 검증; 공백 체크")
    public void NicknameIsBlank() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_BLANK.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 특수문자 테스트")
    public void NicknameHasSpecialChar() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "*ea3sf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getMessage())))
                .andExpect(jsonPath("$.code", is(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER.getCode())))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 검증; 정상적인 반환: 공백과 특수문자가 없는 경우")
    public void NickNameCorrect() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "test33AA가나마"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Jwt 토큰 만료 응답 테스트")
    public void jwtExpiredTest() throws Exception {
        UserRequestDto jwtExpiredDto = UserRequestDto.builder()
                .data(expiredUserJwtData)
                .nickname("testA").role(UserRole.MENTEE)
                .birthYear(1999).birthMonth(3).school("서울과학기술대학교")
                .score(3).scoreRange("후반").graduateDate(2020)
                .major("컴퓨터공학과").subMajor(null).minor(null)
                .field("프론트엔드").techStack("React, Next.js, Redux")
                .career(null).certificate(null).awards(null).activity(null)
                .build();

        mvc.perform(MockMvcRequestBuilders.post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonToString(jwtExpiredDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("1. JWT 토큰이 만료되었습니다. 처음부터 다시 진행해 주세요 ")))
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

}