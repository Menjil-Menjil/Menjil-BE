package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageDto {
    /**
     * 채팅 Message 를 주고받기 위한 DTO
     */
    private String roomId;
    private SenderType senderType;
    private String senderNickname;
    private String message;
    private MessageType messageType;
    private String time;

    @Builder
    private MessageDto(String roomId, SenderType senderType, String senderNickname,
                       String message, MessageType messageType, String time) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }

    public static ChatMessage fromMessageDto(MessageDto messageDto) {
        return ChatMessage.builder()
                .roomId(messageDto.getRoomId())
                .senderType(messageDto.getSenderType())
                .senderNickname(messageDto.getSenderNickname())
                .message(messageDto.getMessage())
                .messageType(messageDto.getMessageType())
                .time(LocalDateTime.parse(messageDto.getTime()))    // convert String -> LocalDateTime
                .build();
    }
}
