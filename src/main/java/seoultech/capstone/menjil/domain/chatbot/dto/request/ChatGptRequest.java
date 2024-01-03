package seoultech.capstone.menjil.domain.chatbot.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chatbot.dto.Message;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatGptRequest {

    private String model;

    private List<Message> messages;

    @Builder
    private ChatGptRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}
