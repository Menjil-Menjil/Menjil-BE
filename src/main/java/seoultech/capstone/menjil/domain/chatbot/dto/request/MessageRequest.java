package seoultech.capstone.menjil.domain.chatbot.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;
import seoultech.capstone.menjil.domain.chatbot.domain.SenderType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageRequest {

    @NotBlank
    private String roomId;
    @NotNull
    private SenderType senderType;
    @NotBlank
    private String senderNickname;
    @NotNull
    private String message;
    @NotNull
    private MessageType messageType;
    @NotBlank
    private String time;

    @Builder
    private MessageRequest(String roomId, SenderType senderType, String senderNickname,
                           String message, MessageType messageType, String time) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }

    public static ChatMessage toChatMessageEntity(MessageRequest messageRequest, LocalDateTime time) {
        return ChatMessage.builder()
                .roomId(messageRequest.getRoomId())
                .senderType(messageRequest.getSenderType())
                .senderNickname(messageRequest.getSenderNickname())
                .message(messageRequest.getMessage())
                .messageType(messageRequest.getMessageType())
                .time(time)
                .build();
    }
}
