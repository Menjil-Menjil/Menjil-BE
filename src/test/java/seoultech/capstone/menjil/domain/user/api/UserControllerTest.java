package seoultech.capstone.menjil.domain.user.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

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
    @DisplayName("닉네임 검증; db 조회 후 중복 닉네임 확인")
    public void NicknameDuplicateDB() throws Exception {

    }

    @Test
    @DisplayName("닉네임 검증; 정상적인 반환")
    public void NickNameCorrect() throws Exception {
        mvc.perform(get("/users/check-nickname")
                        .queryParam("nickname", "test1234"))
                .andExpect(status().isOk())
                .andDo(print());
    }

}