package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.dto.Message;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatGptResponse {
    private String id;
    private String object;
    private Long created;
    private List<Choice> choices;

    private Usage usage;

    public static class Choice {
        private int index;

        private Message message;

        private String finishReason;

        public Message getMessage() {
            return message;
        }
    }

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}