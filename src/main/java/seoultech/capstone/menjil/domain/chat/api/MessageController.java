package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.chat.application.MessageService;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import javax.validation.Valid;

import static seoultech.capstone.menjil.global.exception.ErrorIntValue.TIME_INPUT_INVALID;

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

        // 1. Save Client's chat message and return MessageRespones DTO
        Object result = messageService.saveAndSendClientChatMessage(messageRequest);
        if (result instanceof Integer) {
            // 1-1. error handling
            handleSaveErrorResult((Integer) result, roomId);
            return;
        }

        switch (messageRequest.getMessageType()) {
            case C_QUESTION:
                // 2. Send Client's Chat Message
                sendSuccessResponse(roomId, SuccessCode.MESSAGE_SEND_SUCCESS, (MessageResponse) result);

                // 3. Send AI initial message
                // TODO: 이 부분은 추후 비동기 처리 고려
                if (handleAIMessage(roomId, messageRequest)) return;

                // 4. Send the answer of Client's Question
                handleClientQuestion(roomId, messageRequest);
                break;
            case AI_SELECT:
            case AI_SUMMARY:
            case AI_SUMMARY_ANSWER:
            case AI_SUMMARY_RATING:
            case AI_C_RATING:
                // 2. Send Client's Chat Message
                sendSuccessResponse(roomId, SuccessCode.MESSAGE_SEND_SUCCESS, (MessageResponse) result);
                break;

            default:
                sendMessageTypeErrorResponse(roomId);
                break;
        }
    }

    protected void handleSaveErrorResult(int saveResult, String roomId) {
        ErrorCode code = (saveResult == TIME_INPUT_INVALID.getValue()) ? ErrorCode.TIME_INPUT_INVALID : ErrorCode.INTERNAL_SERVER_ERROR;
        sendErrorResponse(roomId, code);
    }

    protected boolean handleAIMessage(String roomId, MessageRequest messageRequest) {
        MessageResponse response = messageService.sendAIMessage(roomId, messageRequest);
        if (response == null) {
            sendErrorResponse(roomId, ErrorCode.INTERNAL_SERVER_ERROR);
            return true;
        }
        sendSuccessResponse(roomId, SuccessCode.AI_QUESTION_RESPONSE, response);
        return false;
    }

    protected void handleClientQuestion(String roomId, MessageRequest messageRequest) {
        MessageResponse resultResponse = messageService.handleQuestion(roomId, messageRequest);
        if (resultResponse == null) {
            sendErrorResponse(roomId, ErrorCode.INTERNAL_SERVER_ERROR);
        } else {
            sendSuccessResponse(roomId, SuccessCode.AI_QUESTION_RESPONSE, resultResponse);
        }
    }

    protected void sendErrorResponse(String roomId, ErrorCode code) {
        ApiResponse<?> apiResponse = ApiResponse.error(code);
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, apiResponse);
    }

    protected void sendMessageTypeErrorResponse(String roomId) {
        ApiResponse<?> apiResponse = ApiResponse.error(ErrorCode.MESSAGE_TYPE_INPUT_INVALID);
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, apiResponse);
    }

    protected void sendSuccessResponse(String roomId, SuccessCode code, MessageResponse messageResponse) {
        ApiResponse<?> apiResponse = ApiResponse.success(code, messageResponse);
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, apiResponse);
    }
}
