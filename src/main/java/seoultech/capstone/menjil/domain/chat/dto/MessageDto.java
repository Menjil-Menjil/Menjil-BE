package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

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
}
