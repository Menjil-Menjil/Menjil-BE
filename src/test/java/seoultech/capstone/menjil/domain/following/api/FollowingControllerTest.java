package seoultech.capstone.menjil.domain.following.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.following.application.FollowingService;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingQaDto;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserInfoDto;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserInfoResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class FollowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private FollowingService followingService;

    @Test
    void getAllFollowOfUsers() {
    }

    @Test
    @DisplayName("case 1: 정상 로직] 사용자의 정보와 질문답변 내용이 모두 존재한다")
    void getFollowUserInfo() throws Exception {
        // given
        String followNickname = "userA";
        Long answersCount = 2L;
        List<FollowingQaDto> answers = new ArrayList<>();
        answers.add(FollowingQaDto.builder()
                .questionOrigin("원본 질문1")
                .questionSummary("원본 요약1")
                .answer("답변1")
                .answerTime(LocalDateTime.now())
                .likes(2L)
                .views(11L)
                .build());
        answers.add(FollowingQaDto.builder()
                .questionOrigin("원본 질문2")
                .questionSummary("원본 요약2")
                .answer("답변2")
                .answerTime(LocalDateTime.now().plusSeconds(5000))
                .likes(4L)
                .views(10L)
                .build());

        FollowingUserInfoResponse response = createTestFollowingUserInfoResponse(followNickname,
                answersCount, answers);

        // when
        Mockito.when(followingService.getFollowUserInfo(followNickname)).thenReturn(response);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/following/info")
                        .queryParam("followNickname", followNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_FOLLOW_USER_INFO_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_FOLLOW_USER_INFO_SUCCESS.getMessage())))
                .andDo(print());

        verify(followingService, times(1)).getFollowUserInfo(followNickname);
    }


    @Test
    @DisplayName("case 1-1: 정상 로직] 사용자의 정보만 존재하며, 질문답변 내역은 존재하지 않는다.")
    void getFollowUserInfo_qaObject_is_not_existed() throws Exception {
        // given
        String followNickname = "userA";
        Long answersCount = 0L;
        List<FollowingQaDto> answers = new ArrayList<>();

        FollowingUserInfoResponse response = createTestFollowingUserInfoResponse(followNickname,
                answersCount, answers);

        // when
        Mockito.when(followingService.getFollowUserInfo(followNickname)).thenReturn(response);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/following/info")
                        .queryParam("followNickname", followNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_FOLLOW_USER_INFO_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_FOLLOW_USER_INFO_SUCCESS.getMessage())))
                .andDo(print());

        verify(followingService, times(1)).getFollowUserInfo(followNickname);
    }

    @Test
    @DisplayName("case 2: 요청한 사용자의 닉네임이 데이터베이스에 없는 경우")
    void getFollowUserInfo_user_not_in_db() throws Exception {
        // given
        String followNickname = "user333_not_in_db";

        // when
        Mockito.when(followingService.getFollowUserInfo(followNickname))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/following/info")
                        .queryParam("followNickname", followNickname))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())))
                .andDo(print());

        verify(followingService, times(1)).getFollowUserInfo(followNickname);
    }

    private FollowingUserInfoResponse createTestFollowingUserInfoResponse(String followNickname,
                                                                          Long answersCount,
                                                                          List<FollowingQaDto> answers) {
        FollowingUserInfoDto followingUserInfoDto = FollowingUserInfoDto.builder()
                .nickname(followNickname)
                .company("Google")
                .field("백엔드")
                .school("서울과학기술대학교")
                .major("컴퓨터공학과")
                .subMajor(null)
                .minor(null)
                .techStack("Spring Boot, AWS")
                .imgUrl("https://...")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .build();

        return FollowingUserInfoResponse.of(followingUserInfoDto, answersCount, answers);
    }
}