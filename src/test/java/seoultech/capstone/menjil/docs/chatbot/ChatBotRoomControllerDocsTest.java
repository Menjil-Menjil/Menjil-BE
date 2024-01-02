package seoultech.capstone.menjil.docs.chatbot;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import seoultech.capstone.menjil.docs.RestDocsSupport;
import seoultech.capstone.menjil.domain.chatbot.api.ChatBotRoomController;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.ChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.DeleteChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.application.ChatBotRoomService;
import seoultech.capstone.menjil.domain.chatbot.application.dto.request.DeleteChatBotRoomServiceRequest;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatBotRoomController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
public class ChatBotRoomControllerDocsTest extends RestDocsSupport {

    @MockBean
    private ChatBotRoomService chatBotRoomService;

    @Autowired
    private Gson gson;

    /**
     * enterChatBotRoom
     */
    @Test
    @DisplayName("case 1: 요청자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_initiator_Not_In_Db() throws Exception {
        // given
        String initNick = "initNicknameNotExisted";
        String receiverNick = "receiverNicknameExisted";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();

        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()))
                .thenThrow(new CustomException(ErrorCode.INITIATOR_USER_NOT_EXISTED));

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andDo(document("api/chat-bot/room/enter/500/case1",
                        requestFields(
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));

    }

    @Test
    @DisplayName("case 1-1: 응답자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_receiverNick_Not_In_Db() throws Exception {
        // given
        String initNick = "initNicknameExisted";
        String receiverNick = "receiverNicknameNotExisted";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();

        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()))
                .thenThrow(new CustomException(ErrorCode.RECEPIENT_USER_NOT_EXISTED));

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andDo(document("api/chat-bot/room/enter/500/case1-1",
                        requestFields(
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("case 2: 방 입장 요청시 채팅방이 존재하지 않는 경우, 채팅방 및 Welcome Message를 생성 한다. " +
            "그리고 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Not_Exists() throws Exception {
        // given
        String initNick = "userNickname";
        String receiverNick = "receiverUserNickname";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()))
                .thenReturn(new ChatBotRoomIdResponse("new-chat-room-id"));

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("api/chat-bot/room/enter/200/case2",
                        requestFields(
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data.chatBotRoomId").type(JsonFieldType.STRING).description("챗봇 대화방 id")
                        )
                ));

    }

    @Test
    @DisplayName("case 2-1: 방 입장 요청시 채팅방이 이미 존재하는 경우 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Existed() throws Exception {
        // given
        String initNick = "userNickname";
        String receiverNick = "receiverUserNickname";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();
        String content = gson.toJson(roomDto);

        // when
        Mockito.when(chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()))
                .thenReturn(new ChatBotRoomIdResponse("existed-chat-room-id"));

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/enter/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("api/chat-bot/room/enter/200/case2-1",
                        requestFields(
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data.chatBotRoomId").type(JsonFieldType.STRING).description("챗봇 대화방 id")
                        )
                ));
    }

    /**
     * getAllChatBotRooms
     */
    @Test
    @DisplayName("case 1: 기존 챗봇 대화방이 1개 존재하는 경우")
    void getAllChatBotRooms_Number_of_Room_Is_One() throws Exception {
        // given
        String initiatorNickname = "nickname1";
        LocalDateTime now = LocalDateTime.now();
        ChatBotRoomResponse response = ChatBotRoomResponse.
                builder()
                .roomId("room-id")
                .imgUrl("profile/default.png")
                .recipientNickname("상대방 닉네임")
                .createdDateTime(now)
                .questionMessage("질문 1입니다.")
                .questionMessageDateTime(now.minusHours(2))
                .build();

        List<ChatBotRoomResponse> result = new ArrayList<>();
        result.add(response);

        // when
        Mockito.when(chatBotRoomService.getAllChatBotRooms(initiatorNickname))
                .thenReturn(result);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("api/chat-bot/rooms/200/case1",
                        requestParameters(
                                parameterWithName("initiatorNickname").description("사용자 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("대화방 목록 리스트"),
                                fieldWithPath("data[].roomId").type(JsonFieldType.STRING).description("채팅방 ID"),
                                fieldWithPath("data[].recipientNickname").type(JsonFieldType.STRING).description("수신자 닉네임"),
                                fieldWithPath("data[].imgUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
                                fieldWithPath("data[].createdDateTime").type(JsonFieldType.STRING).description("대화방 생성 시간"),
                                fieldWithPath("data[].questionMessage").type(JsonFieldType.STRING).description("질문 메시지"),
                                fieldWithPath("data[].questionMessageDateTime").type(JsonFieldType.STRING).description("질문 메시지가 작성된 날짜와 시간")
                        )
                ));
    }

    @Test
    @DisplayName("case 1-1: 기존 챗봇 대화방이 2개 존재하는 경우")
    void getAllChatBotRooms_Number_Of_Room_Is_Two() throws Exception {
        // given
        String initiatorNickname = "nickname1";
        String receiverNickname1 = "상대방 닉네임1";
        String receiverNickname2 = "상대방 닉네임2";

        LocalDateTime now = LocalDateTime.now();
        ChatBotRoomResponse response1 = ChatBotRoomResponse.
                builder()
                .roomId("room-id1")
                .imgUrl("profile/default.png")
                .recipientNickname(receiverNickname1)
                .createdDateTime(now)
                .questionMessage("질문 1입니다.")
                .questionMessageDateTime(now.minusHours(2))
                .build();
        ChatBotRoomResponse response2 = ChatBotRoomResponse.
                builder()
                .roomId("room-id2")
                .imgUrl("profile/default.png")
                .recipientNickname(receiverNickname2)
                .createdDateTime(now.minusHours(1))
                .questionMessage("질문 2입니다.")
                .questionMessageDateTime(now.minusHours(4))
                .build();

        List<ChatBotRoomResponse> result = new ArrayList<>();
        result.add(response1);
        result.add(response2);

        // when
        Mockito.when(chatBotRoomService.getAllChatBotRooms(initiatorNickname))
                .thenReturn(result);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("api/chat-bot/rooms/200/case1-1",
                        requestParameters(
                                parameterWithName("initiatorNickname").description("사용자 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("대화방 목록 리스트"),
                                fieldWithPath("data[].roomId").type(JsonFieldType.STRING).description("채팅방 ID"),
                                fieldWithPath("data[].recipientNickname").type(JsonFieldType.STRING).description("수신자 닉네임"),
                                fieldWithPath("data[].imgUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
                                fieldWithPath("data[].createdDateTime").type(JsonFieldType.STRING).description("대화방 생성 시간"),
                                fieldWithPath("data[].questionMessage").type(JsonFieldType.STRING).description("질문 메시지"),
                                fieldWithPath("data[].questionMessageDateTime").type(JsonFieldType.STRING).description("질문 메시지가 작성된 날짜와 시간")
                        )
                ));
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
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/chat-bot/rooms/")
                        .queryParam("initiatorNickname", initiatorNickname))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("api/chat-bot/rooms/200/case2",
                        requestParameters(
                                parameterWithName("initiatorNickname").description("사용자 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("대화방 목록 리스트")
                        )
                ));
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
        Mockito.when(chatBotRoomService.quitRoom(Mockito.any(DeleteChatBotRoomServiceRequest.class)))
                .thenReturn(true);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("api/chat-bot/room/quit/201/case1",
                        requestFields(
                                fieldWithPath("roomId").type(JsonFieldType.STRING)
                                        .description("챗봇 대화방 id"),
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터(Null)")
                        )
                ));
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
        Mockito.when(chatBotRoomService.quitRoom(Mockito.any(DeleteChatBotRoomServiceRequest.class)))
                .thenReturn(false);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/chat-bot/room/quit/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andDo(document("api/chat-bot/room/quit/500/case2",
                        requestFields(
                                fieldWithPath("roomId").type(JsonFieldType.STRING)
                                        .description("챗봇 대화방 id"),
                                fieldWithPath("initiatorNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("recipientNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 '대화 시작하기' 요청을 보내는 상대방의 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터(Null)")
                        )
                ));
    }
}
