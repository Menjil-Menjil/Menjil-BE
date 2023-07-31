package seoultech.capstone.menjil.domain.chat.api;

import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.time.LocalDateTime;
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

    @Autowired
    private Gson gson;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;
    @MockBean
    private RoomService roomService;

    /**
     * createRoom()
     */
    @Test
    @DisplayName("채팅방 생성이 정상적으로 진행되었을 때, 201 응답과 그에 해당하는 메시지가 DTO 객체로 리턴된다.")
    void createRoom() throws Exception {
        RoomDto roomDto = new RoomDto("test_room1", "test_mentee1", "test_mentor1");
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.createRoom(roomDto)).thenReturn(HttpStatus.CREATED.value());

        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(201)))
                .andExpect(jsonPath("$.message", is("채팅방이 정상적으로 생성되었습니다")))
                .andExpect(jsonPath("$.data", is("test_room1")))
                .andDo(print());

        verify(roomService, times(1)).createRoom(roomDto);
    }

    @Test
    @DisplayName("채팅방 생성시 RoomService 에서 오류가 발생하면, CustomException 리턴")
    void createRoom_error() throws Exception {
        RoomDto roomDto = new RoomDto("test_room2", "test_mentee2", "test_mentor2");
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.createRoom(roomDto)).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

        mvc.perform(MockMvcRequestBuilders.post("/api/chat/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.SERVER_ERROR.getMessage())))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))  // data is null
                .andDo(print());

        verify(roomService, times(1)).createRoom(roomDto);
    }

    /**
     * enterTheRoom()
     */
    @Test
    @DisplayName("방 입장시 채팅 내역이 존재하지 않는 경우")
    void enterTheRoom_singleMessage() throws Exception {
        String roomId = "test_room_id_1";
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

        Mockito.when(roomService.enterTheRoom(roomId)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/room/enter/" + roomId));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomId);

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
    @DisplayName("방 입장시 채팅 내역이 존재하는 경우")
    void enterTheRoom_multipleMessages() throws Exception {
        String roomId = "test_room_id_1";
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

        Mockito.when(roomService.enterTheRoom(roomId)).thenReturn(messageList);

        // Act
        mvc.perform(MockMvcRequestBuilders.get("/api/chat/room/enter/" + roomId));

        // Assert
        verify(roomService, times(1)).enterTheRoom(roomId);

        // ArgumentCaptor setup
        ArgumentCaptor<ResponseEntity> responseEntityCaptor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(eq("/queue/chat/room/" + roomId), responseEntityCaptor.capture());

        ResponseEntity<ApiResponse<List<MessagesResponse>>> capturedResponseEntity = responseEntityCaptor.getValue();
        assertThat(capturedResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(capturedResponseEntity.getBody().getCode()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getCode());
        assertThat(capturedResponseEntity.getBody().getMessage()).isEqualTo(SuccessCode.MESSAGE_LOAD_SUCCESS.getMessage());
        assertThat(capturedResponseEntity.getBody().getData()).isEqualTo(messageList);
    }

}