package seoultech.capstone.menjil.domain.follow.api;

import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.follow.application.FollowService;
import seoultech.capstone.menjil.domain.follow.api.dto.request.FollowCreateRequest;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.*;


@WebMvcTest(controllers = FollowController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class FollowControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Gson gson;

    @MockBean
    private FollowService followService;

    private final String TEST_USER_NICKNAME = "user_nickname33";
    private final String TEST_FOLLOW_NICKNAME = "follow_nickname33";

    /**
     * createFollow
     */
    @Test
    @DisplayName("case 1: 팔로우가 생성된 경우")
    void createFollow_follow_created() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest())).thenReturn(FOLLOW_CREATED.getValue());

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CREATED.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_CREATED.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).createFollow(followCreateRequest.toServiceRequest());
    }

    @Test
    @DisplayName("case 2: 팔로우가 제거된 경우")
    void createFollow_follow_deleted() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest())).thenReturn(FOLLOW_DELETED.getValue());

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_DELETED.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOW_DELETED.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).createFollow(followCreateRequest.toServiceRequest());
    }

    @Test
    @DisplayName("case 3: 서버 오류")
    void createFollow_INTERNAL_SERVER_ERROR() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest()))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())))
                .andExpect(jsonPath("$.data", is(Matchers.nullValue())))  // Check for null
                .andDo(print());

        verify(followService, times(1)).createFollow(followCreateRequest.toServiceRequest());
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