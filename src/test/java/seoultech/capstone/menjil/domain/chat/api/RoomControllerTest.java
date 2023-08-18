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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfo;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
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

    /**
     * enterTheRoom()
     */
    @Test
    @DisplayName("방 입장시 기존에 채팅방이 존재하지 않는 경우")
    void enterTheRoom_when_room_not_exists() throws Exception {
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .roomId("test_room_id_1")
                .build();

        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(null)
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.enterTheRoom(roomDto)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomDto);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()), responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_CREATED.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_CREATED.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageList);
    }

    @Test
    @DisplayName("방 입장 이후, 두 번째 입장하는 경우(db에 메시지가 하나만 존재하는 경우)")
    void enterTheRoom_when_db_has_one_message() throws Exception {
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .roomId("test_room_id_1")
                .build();

        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageFirstList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(null)    // here is null
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());

        List<MessagesResponse> messageSecondList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(1)   // here is not null
                .roomId(roomDto.getRoomId())
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.enterTheRoom(roomDto))
                .thenReturn(messageFirstList) // the first time enterTheRoom() is called, return messageFirstList
                .thenReturn(messageSecondList); // the second time enterTheRoom() is called, return messageSecondList

        // Act
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verify(roomService, times(2)).enterTheRoom(roomDto);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(2)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()),
                responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageSecondList);
    }

    @Test
    @DisplayName("방 입장시 기존에 채팅방이 존재하며, 채팅 메시지가 1개 보다 많이 존재하는 경우")
    void enterTheRoom_when_db_has_three_message() throws Exception {
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .roomId("test_room_id_1")
                .build();
        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageList = Arrays.asList(
                MessagesResponse.builder()
                        ._id("test_uuid_1")
                        .order(1)
                        .roomId(roomDto.getRoomId())
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now.toString())
                        .build(),
                MessagesResponse.builder()
                        ._id("test_uuid_2")
                        .roomId(roomDto.getRoomId())
                        .order(2)
                        .senderType(SenderType.MENTEE)
                        .senderNickname("test_mentee_nickname")
                        .message("test message 2")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000).toString())
                        .build(),
                MessagesResponse.builder()
                        ._id("test_uuid_3")
                        .roomId(roomDto.getRoomId())
                        .order(3)
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(5000).toString())
                        .build()
        );

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.enterTheRoom(roomDto)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomDto);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomDto.getRoomId()),
                responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageList);
    }

    /**
     * getAllRooms()
     */
    @Test
    @DisplayName("사용자의 전체 방 목록을 불러온다")
    void getAllRooms() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String roomId2 = "test_room_2";
        String roomId3 = "test_room_3";
        String nickname2 = "서울과기대2123";
        String nickname3 = "서울시립1873";
        String type = "MENTEE";

        // when
        Mockito.when(roomService.getAllRooms(targetNickname, type)).thenReturn(Arrays.asList(
                RoomInfo.builder()
                        .roomId(roomId2)
                        .nickname(nickname2)
                        .lastMessage("hello here~~22")
                        .build(),
                RoomInfo.builder()
                        .roomId(roomId3)
                        .nickname(nickname3)
                        .lastMessage("hello there~~33")
                        .build()
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

        verify(roomService, times(1)).getAllRooms(targetNickname, type);
    }

    @Test
    @DisplayName("사용자의 전체 방 목록을 불러오는데, 방 목록이 존재하지 않는 경우")
    void getAllRooms_room_not_exists() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String type = "MENTEE";
        List<RoomInfo> result = new ArrayList<>();

        // when
        Mockito.when(roomService.getAllRooms(targetNickname, type)).thenReturn(result);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/rooms/")
                        .queryParam("nickname", targetNickname)
                        .queryParam("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(roomService, times(1)).getAllRooms(targetNickname, type);
    }

}