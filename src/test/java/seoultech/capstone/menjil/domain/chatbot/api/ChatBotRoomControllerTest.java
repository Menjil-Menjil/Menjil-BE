package seoultech.capstone.menjil.domain.chatbot.api;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.ChatBotRoomDto;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.DeleteChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.application.ChatBotRoomService;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = ChatBotRoomController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class ChatBotRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private ChatBotRoomService chatBotRoomService;

    /**
     * enterChatBotRoom
     */
    @Test
    @DisplayName("case 1: 요청자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_initiator_Not_In_Db() throws Exception {
        // given
        String initNick = "initNicknameNotExisted";
        String receiverNick = "receiverNicknameExisted";
        ChatBotRoomDto roomDto = ChatBotRoomDto.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();

        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto))
                .thenThrow(new CustomException(ErrorCode.INITIATOR_USER_NOT_EXISTED));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(chatBotRoomService, times(1)).enterChatBotRoom(roomDto);
    }

    @Test
    @DisplayName("case 1-1: 응답자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_receiverNick_Not_In_Db() throws Exception {
        // given
        String initNick = "initNicknameExisted";
        String receiverNick = "receiverNicknameNotExisted";
        ChatBotRoomDto roomDto = ChatBotRoomDto.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();

        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto))
                .thenThrow(new CustomException(ErrorCode.RECEPIENT_USER_NOT_EXISTED));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(chatBotRoomService, times(1)).enterChatBotRoom(roomDto);
    }

    @Test
    @DisplayName("case 2: 방 입장 요청시 채팅방이 존재하지 않는 경우, 채팅방 및 WelcomeMsg를 생성 한다. " +
            "그리고 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Not_Exists() throws Exception {
        // given
        String initNick = "userNickname";
        String receiverNick = "receiverUserNickname";
        ChatBotRoomDto roomDto = ChatBotRoomDto.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto))
                .thenReturn(new ChatBotRoomIdResponse("new-chat-room-id"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                        .andDo(print());

        verify(chatBotRoomService, times(1)).enterChatBotRoom(roomDto);
    }

    @Test
    @DisplayName("case 2-1: 방 입장 요청시 채팅방이 이미 존재하는 경우 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Existed() throws Exception {
        // given
        String initNick = "userNickname";
        String receiverNick = "receiverUserNickname";
        ChatBotRoomDto roomDto = ChatBotRoomDto.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto))
                .thenReturn(new ChatBotRoomIdResponse("existed-chat-room-id"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(chatBotRoomService, times(1)).enterChatBotRoom(roomDto);
    }

    /**
     * getAllChatBotRooms
     */
    @Test
    @DisplayName("case 1: 기존 챗봇 대화방이 1개 존재하는 경우")
    void getAllChatBotRooms_Number_of_Room_Is_One() throws Exception {
        // given
        String initiatorNickname = "nickname1";
        ChatBotRoomResponse response = ChatBotRoomResponse.of("chat-room-id", "receiverNickname",
                "profile/default.png");
        List<ChatBotRoomResponse> result = new ArrayList<>();
        result.add(response);

        // when
        Mockito.when(chatBotRoomService.getAllChatBotRooms(initiatorNickname))
                .thenReturn(result);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_CHAT_BOT_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_CHAT_BOT_ROOMS_AVAILABLE.getMessage())))
                .andDo(print());

        verify(chatBotRoomService, times(1)).getAllChatBotRooms(initiatorNickname);
    }

    @Test
    @DisplayName("case 1-1: 기존 챗봇 대화방이 2개 존재하는 경우")
    void getAllChatBotRooms_Number_Of_Room_Is_Two() throws Exception {
        // given
        String initiatorNickname = "nickname1";
        String receiverNickname1 = "receiverNickname1";
        String receiverNickname2 = "receiverNickname2";

        ChatBotRoomResponse response1 = ChatBotRoomResponse.of("chat-room-id1", receiverNickname1,
                "profile/default.png");
        ChatBotRoomResponse response2 = ChatBotRoomResponse.of("chat-room-id2", receiverNickname2,
                "profile/default.png");
        List<ChatBotRoomResponse> result = new ArrayList<>();
        result.add(response1);
        result.add(response2);

        // when
        Mockito.when(chatBotRoomService.getAllChatBotRooms(initiatorNickname))
                .thenReturn(result);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.GET_CHAT_BOT_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.GET_CHAT_BOT_ROOMS_AVAILABLE.getMessage())))
                .andDo(print());

        verify(chatBotRoomService, times(1)).getAllChatBotRooms(initiatorNickname);
    }

    @Test
    @DisplayName("case 2: 기존에 챗봇 대화방이 하나도 존재하지 않은 경우")
    void getAllChatBotRooms_ChatBotRoom_Not_Existed() throws Exception {
        // given
        String initiatorNickname = "nickname1";
        List<ChatBotRoomResponse> result = new ArrayList<>();

        // when
        Mockito.when(chatBotRoomService.getAllChatBotRooms(initiatorNickname))
                .thenReturn(result);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.NO_CHAT_BOT_ROOMS_AVAILABLE.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.NO_CHAT_BOT_ROOMS_AVAILABLE.getMessage())))
                .andDo(print());

        verify(chatBotRoomService, times(1)).getAllChatBotRooms(initiatorNickname);
    }

    /**
     * quitRoom
     */
    @Test
    @DisplayName("case 1: 채팅방과 채팅방에 포함된 대화 메시지가 정상적으로 제거된 경우")
    void quitRoom_success() throws Exception {
        // given
        String roomId = "room-3";
        String initiatorNickname = "nickname1";
        String receiverNickname1 = "receiverNickname1";
        DeleteChatBotRoomRequest request = DeleteChatBotRoomRequest
                .builder()
                .roomId(roomId)
                .initiatorNickname(initiatorNickname)
                .recipientNickname(receiverNickname1)
                .build();

        String content = gson.toJson(request);

        // when
        Mockito.when(chatBotRoomService.quitRoom(Mockito.any(DeleteChatBotRoomRequest.class)))
                .thenReturn(true);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.ROOM_DELETE_SUCCESS.getCode())))
                .andExpect(jsonPath("$.message", is(SuccessCode.ROOM_DELETE_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(chatBotRoomService, times(1)).quitRoom(Mockito.any(DeleteChatBotRoomRequest.class));
    }

    @Test
    @DisplayName("case 2: 서버 내부 오류 등으로 인해, 정상적으로 데이터가 지워지지 못한 경우")
    void quitRoom_fail() throws Exception {
        // given
        String roomId = "room-3";
        String initiatorNickname = "nickname1";
        String receiverNickname1 = "receiverNickname1";
        DeleteChatBotRoomRequest request = DeleteChatBotRoomRequest
                .builder()
                .roomId(roomId)
                .initiatorNickname(initiatorNickname)
                .recipientNickname(receiverNickname1)
                .build();

        String content = gson.toJson(request);

        // when
        Mockito.when(chatBotRoomService.quitRoom(Mockito.any(DeleteChatBotRoomRequest.class)))
                .thenReturn(false);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-bot/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())))
                .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        verify(chatBotRoomService, times(1)).quitRoom(Mockito.any(DeleteChatBotRoomRequest.class));
    }
}