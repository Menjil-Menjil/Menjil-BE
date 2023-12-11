package seoultech.capstone.menjil.domain.main.api;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.application.dto.response.FollowUserResponse;
import seoultech.capstone.menjil.domain.main.application.dto.response.UserInfoResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @MockBean
    private RoomService roomService;

    @MockBean
    private MainPageService mainPageService;

    private final int SIZE = 3;
    private final Sort SORT = Sort.by("createdDate", "nickname").ascending();


    /**
     * getMentors
     */
    @Test
    @DisplayName("page=0일 때, 멘토 데이터가 존재하지 않는 경우 content에 빈 리스트가 리턴되는지 확인한다")
    void getMentors_page_0_mentor_is_not_exist() throws Exception {
        // given
        String nickname = "test_user_1";
        int pageNumber = 0;

        /* 여기서 주의사항: MainPageController의 파라미터 중 @PageableDefault에서 작성한 값과 일치하도록 작성해야 한다 */
        Pageable pageable = PageRequest.of(pageNumber, SIZE, SORT);

        List<UserInfoResponse> responseList = new ArrayList<>();
        Page<UserInfoResponse> page = new PageImpl<>(responseList);

        // when
        Mockito.when(mainPageService.getMentors(nickname, pageable)).thenReturn(page);

        // then
        mvc.perform(get("/api/main/mentors")
                        .queryParam("nickname", nickname)
                        .queryParam("page", String.valueOf(pageable.getPageNumber())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USERS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USERS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data.content").isEmpty())    // 빈 리스트이므로, doesNotExist() (X)
                .andDo(print());

        verify(mainPageService, times(1)).getMentors(nickname, pageable);
    }

    @Test
    @DisplayName("page=0일 때, 멘토 데이터가 2개가 존재하는 경우 정상적으로 2개의 content를 가져오는지 테스트")
    void getMentors_page_0_mentor_2() throws Exception {
        // given
        String nickname = "test_user_1";
        int pageNumber = 0;

        /* 여기서 주의사항: MainPageController의 파라미터 중 @PageableDefault에서 작성한 값과 일치하도록 작성해야 한다 */
        Pageable pageable = PageRequest.of(pageNumber, SIZE, SORT);

        // 현재 UserInfoResponse는 of 메서드가 없으므로,
        // fromUserEntity 메서드를 사용하기 위해, userA, userB 생성
        User userA = createTestUser("google_1231323", "test@google.com", "test_1");
        User userB = createTestUser("google_1231324", "test2@google.com", "test_2");
        List<UserInfoResponse> responseList = List.of(UserInfoResponse.fromUserEntity(userA),
                UserInfoResponse.fromUserEntity(userB));
        Page<UserInfoResponse> page = new PageImpl<>(responseList);

        // when
        Mockito.when(mainPageService.getMentors(nickname, pageable)).thenReturn(page);

        // then
        mvc.perform(get("/api/main/mentors")
                        .queryParam("nickname", nickname)
                        .queryParam("page", String.valueOf(pageable.getPageNumber())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_USERS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_USERS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content[0]").isNotEmpty())
                .andExpect(jsonPath("$.data.content[1]").isNotEmpty())
                .andExpect(jsonPath("$.data.content[2]").doesNotExist()) // Data not exists
                .andDo(print());

        verify(mainPageService, times(1)).getMentors(nickname, pageable);
    }

    /**
     * getAllRoomsOfUser
     */
    @Test
    @DisplayName("사용자의 전체 방 목록을 불러온다")
    void getAllRoomsOfUser() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String roomId2 = "test_room_2";
        String roomId3 = "test_room_3";
        String nickname2 = "서울과기대2123";
        String nickname3 = "서울시립1873";
        String type = "MENTEE";
        LocalDateTime now = LocalDateTime.now();

        // when
        Mockito.when(roomService.getAllRoomsOfUser(targetNickname, type)).thenReturn(Arrays.asList(
                RoomInfoResponse.of(roomId2, nickname2, "test_url", "hello here~~22", now),
                RoomInfoResponse.of(roomId3, nickname3, "test_url", "hello there~~33", now.plusSeconds(1L))
        ));

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/main/rooms/")
                        .queryParam("nickname", targetNickname)
                        .queryParam("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_ROOMS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data[0]").exists())     // check data is not null
                .andExpect(jsonPath("$.data[0].roomId", is(roomId2)))
                .andExpect(jsonPath("$.data[1]").exists())     // check data is not null
                .andExpect(jsonPath("$.data[1].roomId", is(roomId3)))
                .andDo(print());

        verify(roomService, times(1)).getAllRoomsOfUser(targetNickname, type);
    }

    @Test
    @DisplayName("사용자의 채팅방 목록이 존재하지 않는 경우")
    void getAllRoomsOfUser_when_room_not_exists() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String type = "MENTEE";
        List<RoomInfoResponse> result = new ArrayList<>();

        // when
        Mockito.when(roomService.getAllRoomsOfUser(targetNickname, type)).thenReturn(result);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/main/rooms/")
                        .queryParam("nickname", targetNickname)
                        .queryParam("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(roomService, times(1)).getAllRoomsOfUser(targetNickname, type);
    }

    /**
     * getFollowersOfUser
     */
    @Test
    @DisplayName("case 1: 사용자의 팔로우가 5명인 경우")
    void getFollowersOfUser() throws Exception {
        // given
        int FOLLOW_NUM = 5;
        String targetNickname = "test_HOHO";
        List<FollowUserResponse> followUserResponses = IntStream.rangeClosed(1, FOLLOW_NUM)
                .mapToObj(i -> createTestFollowUserResponse(i))
                .collect(Collectors.toList());

        // when
        Mockito.when(mainPageService.getFollowersOfUser(targetNickname)).thenReturn(followUserResponses);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/main/following")
                        .queryParam("nickname", targetNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOWS_EXISTS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOWS_EXISTS.getMessage())))
                .andExpect(jsonPath("$.data[0].nickname", is("nickname_1")))
                .andExpect(jsonPath("$.data[1].nickname", is("nickname_2")))
                .andExpect(jsonPath("$.data[2].nickname", is("nickname_3")))
                .andExpect(jsonPath("$.data[3].nickname", is("nickname_4")))
                .andExpect(jsonPath("$.data[4].nickname", is("nickname_5")))
                .andDo(print());

        verify(mainPageService, times(1)).getFollowersOfUser(targetNickname);
    }

    @Test
    @DisplayName("case 2: 사용자의 팔로우가 0명인 경우")
    void getFollowersOfUser_follow_is_none() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        List<FollowUserResponse> followUserResponses = new ArrayList<>();

        // when
        Mockito.when(mainPageService.getFollowersOfUser(targetNickname)).thenReturn(followUserResponses);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/main/following")
                        .queryParam("nickname", targetNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOWS_NOT_EXISTS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.FOLLOWS_NOT_EXISTS.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(mainPageService, times(1)).getFollowersOfUser(targetNickname);
    }


    private User createTestUser(String id, String email, String nickname) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("컴퓨터공학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .imgUrl("default/profile.png")  // set img url
                .build();
    }

    private FollowUserResponse createTestFollowUserResponse(int index) {
        return new FollowUserResponse("nickname_" + index,
                "company", "techStack", "imgUrl");
    }
}