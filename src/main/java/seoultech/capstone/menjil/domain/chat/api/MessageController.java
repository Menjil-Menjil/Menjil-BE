package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        Object result = "";
        if (MessageType.QUESTION.equals(messageRequest.getMessageType())) {
            // 이 부분은 추후 비동기 처리 고려
            MessageResponse responseDto = messageService.sendAIMessage(roomId, messageRequest);
            simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId,
                    ResponseEntity.status(HttpStatus.OK)
                            .body(ApiResponse.success(SuccessCode.AI_QUESTION_RESPONSE, responseDto)));

            result = messageService.handleQuestion(roomId, messageRequest);
        }

        // /queue/chat/room/{room id}로 메세지 보냄
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, result);
    }

    // 메시지 테스트를 위한 메소드. 테스트 이후 제거할 계획
    // produces에 UTf-8을 해줘야 POST맨에서 chatGPT 응답 결과 한글 메시지가 안 깨짐
    /*@PostMapping(value = "/api/msg", produces = "application/json;charset=utf-8")
    public Object messageTest(@RequestBody MessageRequest messageRequestDto) {
        Object result = messageService.handleQuestion(messageRequestDto.getRoomId(), messageRequestDto);
        System.out.println("result = " + result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.AI_QUESTION_RESPONSE, result));
    }

    // 저장 시간 테스트. 테스트 이후 제거할 계획
    @PostMapping(value = "/api/msg2", produces = "application/json;charset=utf-8")
    public Object saveTest(@RequestBody MessageRequest messageRequestDto) {
        Object result = messageService.sendAIMessage(messageRequestDto.getRoomId(), messageRequestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.AI_QUESTION_RESPONSE, result));
    }*/
}
