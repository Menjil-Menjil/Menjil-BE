package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import seoultech.capstone.menjil.domain.chat.domain.Message;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageDto {
    /**
     * 채팅 Message 를 주고받기 위한 DTO
     */
    private String roomId;
    private String senderNickname;
    private String message;
    private MessageType messageType;
    private String time;

    @Builder
    private MessageDto(String roomId, String senderNickname,
                       String message, MessageType messageType, String time) {
        this.roomId = roomId;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }

    public static MessageDto fromMessage(Message message) {

        // remove nano seconds
        String time = message.getTime().withNano(0).toString();
        time = time.replace("T", " ");

        return MessageDto.builder()
                .roomId(message.getRoomId())
                .senderNickname(message.getSenderNickname())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .time(time)
                .build();
    }
}
