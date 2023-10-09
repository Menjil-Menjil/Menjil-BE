package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageResponse {
    /**
     * 채팅 메시지를 주고받을 때 사용하는 Response DTO
     */
    private String _id;
    private String roomId;
    private SenderType senderType;
    private String senderNickname;
    private String message;
    private Object messageList;
    private MessageType messageType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime time;

    @Builder
    private MessageResponse(String _id, String roomId, SenderType senderType,
                            String senderNickname, String message, Object messageList,
                            MessageType messageType, LocalDateTime time) {
        this._id = _id;
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageList = messageList;
        this.messageType = messageType;
        this.time = time;
    }

    public static MessageResponse fromChatMessageEntity(ChatMessage chatMessage) {
        return MessageResponse.builder()
                ._id(chatMessage.get_id())
                .roomId(chatMessage.getRoomId())
                .senderType(chatMessage.getSenderType())
                .senderNickname(chatMessage.getSenderNickname())
                .message(chatMessage.getMessage())
                .messageList(chatMessage.getMessageList())
                .messageType(chatMessage.getMessageType())
                .time(chatMessage.getTime())
                .build();
    }

}
