package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.chat.application.MessageService;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat/room/{roomId}") // 실제론 메세지 매핑으로 pub/chat/room/{roomId} 임
//    @SendTo("/queue/chat/{roomId}")
    public void enter(@DestinationVariable("roomId") String roomId,
                      @Valid @RequestBody MessageRequest messageRequest) {
        int TIME_INPUT_INVALID = 0;
        int INTERNAL_SERVER_ERROR = 1;
        int SAVE_SUCCESS = 100;

        if (MessageType.QUESTION.equals(messageRequest.getMessageType())) {
            // 1. save Chat Message
            int saveChatMessage = messageService.saveChatMessage(messageRequest);
            ApiResponse<?> errorApiResponse;

            // Exception Handling
            if (saveChatMessage == TIME_INPUT_INVALID) {
                errorApiResponse = ApiResponse.error(ErrorCode.TIME_INPUT_INVALID);
                simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, errorApiResponse);
                return;
            } else if (saveChatMessage == INTERNAL_SERVER_ERROR) {
                errorApiResponse = ApiResponse.error(ErrorCode.SERVER_ERROR);
                simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, errorApiResponse);
                return;
            }

            // 2. Send AI message
            // 이 부분은 추후 비동기 처리 고려
            MessageResponse response = messageService.sendAIMessage(roomId, messageRequest);
            ApiResponse<?> apiResponse = null;
            if (response == null) {
                apiResponse = ApiResponse.error(ErrorCode.SERVER_ERROR);
            } else {
                apiResponse = ApiResponse.success(SuccessCode.AI_QUESTION_RESPONSE, response);
            }
            simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, apiResponse);

            // 3. Handle Question
            MessageResponse resultResponse = messageService.handleQuestion(roomId, messageRequest);
            ApiResponse<?> apiResponse2 = null;
            if (resultResponse == null) {
                apiResponse2 = ApiResponse.error(ErrorCode.SERVER_ERROR);
            } else {
                apiResponse2 = ApiResponse.success(SuccessCode.AI_QUESTION_RESPONSE, resultResponse);
            }
            // /queue/chat/room/{room id}로 메세지 보냄
            simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, apiResponse2);
        }

    }
}
