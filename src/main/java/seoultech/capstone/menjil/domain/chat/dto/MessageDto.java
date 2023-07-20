package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;

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

    public static MessageDto fromMessage(ChatMessage chatMessage) {

        // remove nano seconds
        String time = chatMessage.getTime().withNano(0).toString();
        time = time.replace("T", " ");

        return MessageDto.builder()
                .roomId(chatMessage.getRoomId())
                .senderNickname(chatMessage.getSenderNickname())
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .time(time)
                .build();
    }
}
