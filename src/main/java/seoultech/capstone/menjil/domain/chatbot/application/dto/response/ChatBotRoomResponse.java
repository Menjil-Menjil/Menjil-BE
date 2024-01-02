package seoultech.capstone.menjil.domain.chatbot.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatBotRoomResponse {

    private String roomId;
    private String recipientNickname;
    private String imgUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDateTime;

    private String questionMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime questionMessageDateTime;

    @Builder
    private ChatBotRoomResponse(String roomId, String recipientNickname,
                               String imgUrl, LocalDateTime createdDateTime,
                                String questionMessage, LocalDateTime questionMessageDateTime) {
        this.roomId = roomId;
        this.recipientNickname = recipientNickname;
        this.imgUrl = imgUrl;
        this.createdDateTime = createdDateTime;
        this.questionMessage = questionMessage;
        this.questionMessageDateTime = questionMessageDateTime;
    }
}
