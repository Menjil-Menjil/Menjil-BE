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
import seoultech.capstone.menjil.domain.chat.dto.response.RoomListResponse;
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
    void roomNotExists() throws Exception {
        String roomId = "test_room_id_1";
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .build();
        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(null)
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.createUUID(roomDto)).thenReturn(roomId);
        Mockito.when(roomService.enterTheRoom(roomDto, roomId)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomDto, roomId);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomId), responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_CREATED.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_CREATED.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageList);
    }

    @Test
    @DisplayName("방 입장 이후, 두 번째 입장하는 경우(db에 메시지가 하나만 존재하는 경우)")
    void roomExistsAndMessageHasOne() throws Exception {
        String roomId = "test_room_id_1";
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .build();
        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageFirstList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(null)    // Null
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());
        List<MessagesResponse> messageSecondList = Collections.singletonList(MessagesResponse
                .builder()
                ._id("test_uuid_3")
                .order(1)   // Not Null
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build());

        Gson gson = new Gson();
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.createUUID(roomDto)).thenReturn(roomId);
        Mockito.when(roomService.enterTheRoom(roomDto, roomId))
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
        verify(roomService, times(2)).enterTheRoom(roomDto, roomId);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(2)).convertAndSend(eq("/queue/chat/room/" + roomId), responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageSecondList);
    }

    @Test
    @DisplayName("방 입장시 기존에 채팅방이 존재하며, 채팅 메시지가 1개 보다 많이 존재하는 경우")
    void enterTheRoom_multipleMessages() throws Exception {
        String roomId = "test_room_id_1";
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor_1")
                .menteeNickname("test_mentee_1")
                .build();
        LocalDateTime now = LocalDateTime.now();
        List<MessagesResponse> messageList = Arrays.asList(
                MessagesResponse.builder()
                        ._id("test_uuid_1")
                        .order(1)
                        .roomId(roomId)
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now.toString())
                        .build(),
                MessagesResponse.builder()
                        ._id("test_uuid_2")
                        .roomId(roomId)
                        .order(2)
                        .senderType(SenderType.MENTEE)
                        .senderNickname("test_mentee_nickname")
                        .message("test message 2")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000).toString())
                        .build(),
                MessagesResponse.builder()
                        ._id("test_uuid_3")
                        .roomId(roomId)
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

        Mockito.when(roomService.createUUID(roomDto)).thenReturn(roomId);
        Mockito.when(roomService.enterTheRoom(roomDto, roomId)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room/enter/")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomDto, roomId);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomId), responseEntityCaptor.capture());

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
    void getAllRoomsSucceed() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String nickname2 = "서울과기대2123";
        String nickname3 = "서울시립1873";
        String type = "MENTEE";

        Mockito.when(roomService.getAllRooms(targetNickname, type)).thenReturn(Arrays.asList(
                RoomListResponse.builder()
                        .nickname(nickname2)
                        .lastMessage("hello here~~")
                        .build(),
                RoomListResponse.builder()
                        .nickname(nickname3)
                        .lastMessage("hello there~~")
                        .build()
        ));

        // Act
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/rooms/")
                        .queryParam("nickname", targetNickname)
                        .queryParam("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_ROOMS_AVAILABLE.getMessage())))
                .andExpect(jsonPath("$.data").exists())     // check data is not null
                .andDo(print());

        // Assert
        verify(roomService, times(1)).getAllRooms(targetNickname, type);
    }

    @Test
    @DisplayName("사용자의 전체 방 목록을 불러오는데, 방 목록이 존재하지 않는 경우")
    void getAllRoomsNotExisted() throws Exception {
        // given
        String targetNickname = "test_HOHO";
        String type = "MENTEE";
        List<RoomListResponse> result = new ArrayList<>();

        Mockito.when(roomService.getAllRooms(targetNickname, type)).thenReturn(result);

        // Act
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/rooms/")
                        .queryParam("nickname", targetNickname)
                        .queryParam("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_ROOMS_AND_NOT_EXISTS.getMessage())))
                .andExpect(jsonPath("$.data", is(result)))
                .andDo(print());

        // Assert
        verify(roomService, times(1)).getAllRooms(targetNickname, type);
    }

}