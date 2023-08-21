package seoultech.capstone.menjil.domain.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageRequestDto {

    @NotBlank
    private String roomId;
    @NotBlank
    private SenderType senderType;
    @NotBlank
    private String senderNickname;
    @NotNull
    private String message;
    @NotBlank
    private MessageType messageType;
    @NotBlank
    private String time;

    @Builder
    private MessageRequestDto(String roomId, SenderType senderType, String senderNickname,
                              String message, MessageType messageType, String time) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }

    public static ChatMessage toChatMessage(MessageRequestDto messageRequestDto, LocalDateTime time) {
        return ChatMessage.builder()
                .roomId(messageRequestDto.getRoomId())
                .senderType(messageRequestDto.getSenderType())
                .senderNickname(messageRequestDto.getSenderNickname())
                .message(messageRequestDto.getMessage())
                .messageType(messageRequestDto.getMessageType())
                .time(time)
                .build();
    }
}
