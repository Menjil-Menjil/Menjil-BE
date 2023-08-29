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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;
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

    private final int SIZE = 3;
    private final Sort SORT = Sort.by("createdDate", "nickname").ascending();


    /**
     * getMentorList()
     */
    @Test
    @DisplayName("page=0일 때, 멘토 데이터가 존재하지 않는 경우 content에 빈 리스트가 리턴되는지 확인한다")
    void getMentorList_page_0_mentor_is_not_exist() throws Exception {
        // given
        String nickname = "test_user_1";
        int pageNumber = 0;

        /* 여기서 주의사항: MainPageController의 파라미터 중 @PageableDefault에서 작성한 값과 일치하도록 작성해야 한다 */
        Pageable pageable = PageRequest.of(pageNumber, SIZE, SORT);

        List<MentorInfoResponse> responseList = new ArrayList<>();
        Page<MentorInfoResponse> page = new PageImpl<>(responseList);

        // when
        Mockito.when(mainPageService.getMentorList(nickname, pageable)).thenReturn(page);

        // then
        mvc.perform(get("/api/main/mentors")
                        .queryParam("nickname", nickname)
                        .queryParam("page", String.valueOf(pageable.getPageNumber())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_MENTOR_LIST_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_MENTOR_LIST_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data.content").isEmpty())    // 빈 리스트이므로, doesNotExist() (X)
                .andDo(print());

        Mockito.verify(mainPageService).getMentorList(nickname, pageable);
    }

    @Test
    @DisplayName("page=0일 때, 멘토 데이터가 2개가 존재하는 경우 정상적으로 2개의 content를 가져오는지 테스트")
    void getMentorList_page_0_mentor_2() throws Exception {
        // given
        String nickname = "test_user_1";
        int pageNumber = 0;

        /* 여기서 주의사항: MainPageController의 파라미터 중 @PageableDefault에서 작성한 값과 일치하도록 작성해야 한다 */
        Pageable pageable = PageRequest.of(pageNumber, SIZE, SORT);

        // 현재 MentorInfoResponse는 of 메서드가 없으므로,
        // fromUserEntity 메서드를 사용하기 위해, userA, userB 생성
        User userA = createUser("google_1231323", "test@google.com", "test_1", UserRole.MENTOR);
        User userB = createUser("google_1231324", "test2@google.com", "test_2", UserRole.MENTOR);
        List<MentorInfoResponse> responseList = List.of(MentorInfoResponse.fromUserEntity(userA),
                MentorInfoResponse.fromUserEntity(userB));
        Page<MentorInfoResponse> page = new PageImpl<>(responseList);

        // when
        Mockito.when(mainPageService.getMentorList(nickname, pageable)).thenReturn(page);

        // then
        mvc.perform(get("/api/main/mentors")
                        .queryParam("nickname", nickname)
                        .queryParam("page", String.valueOf(pageable.getPageNumber())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_MENTOR_LIST_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_MENTOR_LIST_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content[0]").isNotEmpty())
                .andExpect(jsonPath("$.data.content[1]").isNotEmpty())
                .andExpect(jsonPath("$.data.content[2]").doesNotExist()) // Data not exists
                .andDo(print());

        Mockito.verify(mainPageService).getMentorList(nickname, pageable);
    }

    /**
     * getAllRoomsOfUser()
     */
    @Test
    @DisplayName("mainPageService의 roomInfoList의 결과로 빈 List가 반환되는 경우(생성된 채팅방이 존재하지 않는 경우)")
    void getUserInfo_room_does_not_exists_in_db() throws Exception {
        // given
        String nickname = "test1";
        List<RoomInfoResponse> roomList = new ArrayList<>();

        // when
        Mockito.when(mainPageService.getAllRoomsOfUser(nickname)).thenReturn(roomList);

        // then
        mvc.perform(get("/api/main/userinfo")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USER_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USER_ROOMS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())   // 빈 리스트
                .andDo(print());

        verify(mainPageService, times(1)).getAllRoomsOfUser(nickname);
    }

    @Test
    @DisplayName("mainPageService의 roomInfoList의 결과로 방의 정보가 담긴 List가 반환되는 경우(생성된 채팅방이 존재하는 경우)")
    void getUserInfo_room_exists_in_db() throws Exception {
        // given
        String nickname = "test1";
        LocalDateTime now = LocalDateTime.now();
        List<RoomInfoResponse> roomList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            roomList.add(RoomInfoResponse.of("room_" + i, nickname, "test_url",
                    "message_" + i, now.plusSeconds(1000L)));
        }

        // when
        // 내가 넣는 대로 Mocking되므로, 방의 순서를 보장하지는 않는다.(내림차순)
        Mockito.when(mainPageService.getAllRoomsOfUser(nickname)).thenReturn(roomList);

        // then
        mvc.perform(get("/api/main/userinfo")
                        .queryParam("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USER_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USER_ROOMS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data").isNotEmpty())   // 데이터가 존재하는 리스트
                .andDo(print());

        verify(mainPageService, times(1)).getAllRoomsOfUser(nickname);
    }

    private User createUser(String id, String email, String nickname, UserRole role) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .role(role).birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("컴퓨터공학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .optionInfo(null)
                .imgUrl("default/profile.png")  // set img url
                .build();
    }
}