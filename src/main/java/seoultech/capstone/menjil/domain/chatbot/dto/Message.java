package seoultech.capstone.menjil.domain.chatbot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Message {
    /**
     * ChatGptRequest, ChatGptResponse 에서 사용됨.
     */
    private String role;
    private String content;

    @Builder
    private Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
