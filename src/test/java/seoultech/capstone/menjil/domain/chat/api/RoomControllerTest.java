package seoultech.capstone.menjil.domain.chat.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageOrderResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = RoomController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class RoomControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private RoomService roomService;

    private final String TEST_ROOM_ID = "test_room_1";
    private final String TEST_MENTEE_NICKNAME = "test_mentee_1";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_1";

    /**
     * enterTheRoom()
     */
    @Test
    @DisplayName("case 1: 방 입장시 기존에 채팅방이 존재하며, 채팅 메시지가 1개보다 많이 존재하는 경우")
    void enterTheRoom_when_db_has_three_message() throws Exception {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        LocalDateTime now = LocalDateTime.now();
        List<MessageOrderResponse> messageOrderResponses = Arrays.asList(
                MessageOrderResponse.builder()
                        ._id("test_uuid_1")
                        .order(1)
                        .roomId(roomDto.getRoomId())
                        .senderType(SenderType.MENTOR)
                        .senderNickname(roomDto.getMentorNickname())
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now)
                        .build(),
                MessageOrderResponse.builder()
                        ._id("test_uuid_2")
                        .roomId(roomDto.getRoomId())
                        .order(2)
                        .senderType(SenderType.MENTEE)
                        .senderNickname(roomDto.getMenteeNickname())
                        .message("test message 2")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000))
                        .build(),
                MessageOrderResponse.builder()
                        ._id("test_uuid_3")
                        .roomId(roomDto.getRoomId())
                        .order(3)
                        .senderType(SenderType.MENTOR)
                        .senderNickname(roomDto.getMentorNickname())
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(5000))
                        .build()
        );

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(roomService.enterTheRoom(roomDto)).thenReturn(messageOrderResponses);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roomService, times(1)).enterTheRoom(roomDto);

        // ArgumentCaptor setup
        ArgumentCaptor<ApiResponse<List<MessageResponse>>> argumentCaptor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()), argumentCaptor.capture());
        ApiResponse<List<MessageResponse>> capturedApiResponse = argumentCaptor.getValue();

        // Here you can validate that `capturedApiResponse` contains the data you expect
        assertThat(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode()).isEqualTo(capturedApiResponse.getCode());
        assertThat(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage()).isEqualTo(capturedApiResponse.getMessage());

        assertThat(messageOrderResponses).isEqualTo(capturedApiResponse.getData());
    }


    @Test
    @DisplayName("case 2: 처음 방에 입장하는 경우")
    void enterTheRoom_SingleMessage_FirstTime() throws Exception {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        LocalDateTime now = LocalDateTime.now();
        List<MessageOrderResponse> memessageOrderResponsessageResponse = Collections.singletonList(MessageOrderResponse.builder()
                ._id("test_uuid_1")
                .order(null)
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname(roomDto.getMentorNickname())
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(roomService.enterTheRoom(roomDto)).thenReturn(memessageOrderResponsessageResponse);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(roomService, times(1)).enterTheRoom(roomDto);

        ArgumentCaptor<ApiResponse<List<MessageResponse>>> argumentCaptor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()), argumentCaptor.capture());
        ApiResponse<List<MessageResponse>> capturedApiResponse = argumentCaptor.getValue();

        // Here you can validate that `capturedApiResponse` contains the data you expect
        assertThat(SuccessCode.MESSAGE_CREATED.getCode()).isEqualTo(capturedApiResponse.getCode());
        assertThat(SuccessCode.MESSAGE_CREATED.getMessage()).isEqualTo(capturedApiResponse.getMessage());

        assertThat(memessageOrderResponsessageResponse).isEqualTo(capturedApiResponse.getData());
    }

    @Test
    @DisplayName("case 3: 방 입장 이후 따로 대화를 입력하지 않은 상황에서, 다시 입장하는 경우(db에 메시지가 하나만 존재하는 경우)")
    void enterTheRoom_SingleMessage_NotFirstTime() throws Exception {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        LocalDateTime now = LocalDateTime.now();
        List<MessageOrderResponse> firstMessageResponse = Collections.singletonList(MessageOrderResponse.builder()
                ._id("test_uuid_3")
                .order(null)    // here is null
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname(roomDto.getMentorNickname())
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        List<MessageOrderResponse> secondMessageResponse = Collections.singletonList(MessageOrderResponse.builder()
                ._id("test_uuid_3")
                .order(1)   // here is not null
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname(roomDto.getMentorNickname())
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(roomService.enterTheRoom(roomDto))
                .thenReturn(firstMessageResponse) // the first time enterTheRoom() is called, return firstMessageResponse
                .thenReturn(secondMessageResponse); // the second time enterTheRoom() is called, return secondMessageResponse

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        verify(roomService, times(2)).enterTheRoom(roomDto);

        ArgumentCaptor<ApiResponse<List<MessageResponse>>> argumentCaptor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(simpMessagingTemplate, times(2)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()), argumentCaptor.capture());
        ApiResponse<List<MessageResponse>> capturedApiResponse = argumentCaptor.getValue();

        // Here you can validate that `capturedApiResponse` contains the data you expect
        assertThat(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode()).isEqualTo(capturedApiResponse.getCode());
        assertThat(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage()).isEqualTo(capturedApiResponse.getMessage());

        // secondMessageResponse
        assertThat(secondMessageResponse).isEqualTo(capturedApiResponse.getData());
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
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/rooms/")
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
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/rooms/")
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
     * quitRoom
     */
    @Test
    @DisplayName("case 1: 정상적으로 대화방을 나온 경우")
    void quitRoom_success() throws Exception {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(roomService.quitRoom(roomDto)).thenReturn(true);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.ROOM_DELETE_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.ROOM_DELETE_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(roomService, times(1)).quitRoom(roomDto);
    }

    @Test
    @DisplayName("case 2: 서버 내부 오류 등으로 인해, 정상적으로 데이터가 지워지지 못한 경우")
    void quitRoom_fail() throws Exception {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(roomService.quitRoom(roomDto)).thenReturn(false);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(roomService, times(1)).quitRoom(roomDto);
    }
}