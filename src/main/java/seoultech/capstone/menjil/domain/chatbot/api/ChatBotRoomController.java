package seoultech.capstone.menjil.domain.chatbot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.ChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.DeleteChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.application.ChatBotRoomService;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@EnableWebMvc
@RequestMapping("/api/chat-bot")
public class ChatBotRoomController {

    private final ChatBotRoomService chatBotRoomService;

    /**
     * 챗봇 대화방에 입장한다.
     */
    @PostMapping("/room/enter")
    public ResponseEntity<ApiResponse<ChatBotRoomIdResponse>> enterChatBotRoom(@RequestBody ChatBotRoomRequest chatBotRoomRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_CHAT_BOT_ROOM_AVAILABLE,
                        chatBotRoomService.enterChatBotRoom(chatBotRoomRequest.toServiceRequest())));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatBotRoomResponse>>> getAllChatBotRooms(
            @RequestParam("initiatorNickname") String initiatorNickname) {
        List<ChatBotRoomResponse> result = chatBotRoomService.getAllChatBotRooms(initiatorNickname);

        if (userHasNoRooms(result)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.NO_CHAT_BOT_ROOMS_AVAILABLE, result));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.GET_CHAT_BOT_ROOMS_AVAILABLE, result));
        }
    }

    @PostMapping("/room/quit")
    public ResponseEntity<ApiResponse<?>> quitRoom(@RequestBody DeleteChatBotRoomRequest request) {
        boolean result = chatBotRoomService.quitRoom(request.toServiceRequest());
        if (result) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(SuccessCode.ROOM_DELETE_SUCCESS));
        } else {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean userHasNoRooms(List<ChatBotRoomResponse> list) {
        return list.isEmpty();
    }
}
