package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    //    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/hello")
    @SendTo("/sub/greetings")
    public String chat(String message) {
//        simpMessagingTemplate.convertAndSend("/roomId/" + chatMessage.getRoomId(),
//                chatMessage.getMessage());

        return "당신이 입력한 메시지는 : " + message + " 입니다.!!!!!!";
    }

}
