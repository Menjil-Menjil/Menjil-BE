package seoultech.capstone.menjil.domain.main.api;

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
import org.springframework.test.web.servlet.MockMvc;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfo;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfo;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = MainPageController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class MainPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Gson gson;

    @MockBean
    private MainPageService mainPageService;

    /**
     * getUserInfo()
     */
    @Test
    @DisplayName("mainPageService의 roomInfoList의 결과로 빈 List가 반환되는 경우(생성된 채팅방이 존재하지 않는 경우)")
    void getUserInfo_room_does_not_exists_in_db() throws Exception {
        // given
        String nickname = "test1";
        UserInfo userInfo = UserInfo.builder()
                .nickname(nickname)
                .school("서울과학기술대학교")
                .major("컴퓨터공학과")
                .imgUrl("test_url")
                .build();
        List<RoomInfo> roomList = new ArrayList<>();

        // when
        Mockito.when(mainPageService.getUserInfo(nickname)).thenReturn(userInfo);
        Mockito.when(mainPageService.getUserRoomList(nickname)).thenReturn(roomList);

        // then
        mvc.perform(get("/api/main/userinfo")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USER_INFO_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USER_INFO_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data.userInfo.nickname", is(nickname)))
                .andExpect(jsonPath("$.data.roomInfoList").isEmpty())   // 빈 리스트
                .andDo(print());

        verify(mainPageService, times(1)).getUserInfo(nickname);
        verify(mainPageService, times(1)).getUserRoomList(nickname);
    }

    @Test
    @DisplayName("mainPageService의 roomInfoList의 결과로 방의 정보가 담긴 List가 반환되는 경우(생성된 채팅방이 존재하는 경우)")
    void getUserInfo_room_exists_in_db() throws Exception {
        // given
        String nickname = "test1";
        UserInfo userInfo = UserInfo.builder()
                .nickname(nickname)
                .school("서울과학기술대학교")
                .major("컴퓨터공학과")
                .imgUrl("test_url")
                .build();

        List<RoomInfo> roomList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().withNano(0);    // ignore milliseconds
        for (int i = 1; i <= 3; i++) {
            roomList.add(RoomInfo.builder()
                    .roomId("room_" + i)
                    .nickname(nickname)
                    .imgUrl("test_url")
                    .lastMessage("message_" + i)
                    .lastMessageTime(now.plusSeconds(i * 1000L))
                    .build());
        }

        // when
        Mockito.when(mainPageService.getUserInfo(nickname)).thenReturn(userInfo);

        // 내가 넣는 대로 Mocking되므로, 방의 순서를 보장하지는 않는다.(내림차순)
        Mockito.when(mainPageService.getUserRoomList(nickname)).thenReturn(roomList);

        // then
        mvc.perform(get("/api/main/userinfo")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USER_INFO_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USER_INFO_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data.userInfo.nickname", is(nickname)))
                .andExpect(jsonPath("$.data.roomInfoList").isNotEmpty())   // 데이터가 존재하는 리스트
                .andDo(print());

        verify(mainPageService, times(1)).getUserInfo(nickname);
        verify(mainPageService, times(1)).getUserRoomList(nickname);
    }
}