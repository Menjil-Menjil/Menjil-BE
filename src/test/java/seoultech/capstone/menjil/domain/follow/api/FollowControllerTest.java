package seoultech.capstone.menjil.domain.follow.api;

import com.google.gson.Gson;
import org.hamcrest.Matchers;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.follow.application.FollowService;
import seoultech.capstone.menjil.domain.follow.dto.request.FollowRequest;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = FollowController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class FollowControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FollowService followService;

    private final String TEST_USER_NICKNAME = "test_user_nickname";
    private final String TEST_FOLLOW_NICKNAME = "test_follow_nickname";
    private final int FOLLOW_CREATED = 0;
    private final int FOLLOW_DELETED = 1;
    private final int INTERNAL_SERVER_ERROR = -100;

    /**
     * followRequest
     */
    @Test
    @DisplayName("case 1: 팔로우가 생성된 경우")
    void followRequest_follow_created() throws Exception {
        // given
        FollowRequest followRequest = FollowRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        Gson gson = new Gson();
        String content = gson.toJson(followRequest);

        // when
        Mockito.when(followService.followRequest(followRequest)).thenReturn(FOLLOW_CREATED);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CREATED.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_CREATED.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).followRequest(followRequest);
    }

    @Test
    @DisplayName("case 2: 팔로우가 제거된 경우")
    void followRequest_follow_deleted() throws Exception {
        // given
        FollowRequest followRequest = FollowRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        Gson gson = new Gson();
        String content = gson.toJson(followRequest);

        // when
        Mockito.when(followService.followRequest(followRequest)).thenReturn(FOLLOW_DELETED);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_DELETED.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_DELETED.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).followRequest(followRequest);
    }

    @Test
    @DisplayName("case 3: 서버 오류")
    void followRequest_internal_server_error() throws Exception {
        // given
        FollowRequest followRequest = FollowRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        Gson gson = new Gson();
        String content = gson.toJson(followRequest);

        // when
        Mockito.when(followService.followRequest(followRequest)).thenReturn(INTERNAL_SERVER_ERROR);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.SERVER_ERROR.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).followRequest(followRequest);
    }

    /**
     * checkFollowStatus
     */
    @Test
    @DisplayName("case 1: follow가 이미 존재하는 경우")
    void checkFollowStatus_return_true() throws Exception {
        // given
        boolean FOLLOW_EXISTS = true;

        // when
        Mockito.when(followService.checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME)).thenReturn(FOLLOW_EXISTS);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/follow/check-status/")
                        .queryParam("userNickname", TEST_USER_NICKNAME)
                        .queryParam("followNickname", TEST_FOLLOW_NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data").value(is(FOLLOW_EXISTS)))
                .andDo(print());

        verify(followService, times(1)).checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
    }

    @Test
    @DisplayName("case 2: follow가 존재하지 않는 경우")
    void checkFollowStatus_return_false() throws Exception {
        // given
        boolean FOLLOW_NOT_EXISTS = false;

        // when
        Mockito.when(followService.checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME)).thenReturn(FOLLOW_NOT_EXISTS);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/follow/check-status/")
                        .queryParam("userNickname", TEST_USER_NICKNAME)
                        .queryParam("followNickname", TEST_FOLLOW_NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data").value(is(FOLLOW_NOT_EXISTS)))
                .andDo(print());

        verify(followService, times(1)).checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
    }
}