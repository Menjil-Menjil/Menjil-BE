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
public class MessageListResponse {
    /**
     * 사용자가 대화방에 입장 시 채팅 메시지 내용을 불러오는 객체
     * MessageResponse를 사용하였으나, order와 id 값의 포함 유무 차이 때문에 분리하였음
     */
    private String _id;
    private Integer order;
    @JsonIgnore // ignore room id
    private String roomId;
    private SenderType senderType;
    private String senderNickname;
    private String message;
    private Object messageList;
    private MessageType messageType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime time;

    @Builder
    private MessageListResponse(String _id, Integer order, String roomId, SenderType senderType,
                                String senderNickname, String message, Object messageList,
                                MessageType messageType, LocalDateTime time) {
        this._id = _id;
        this.order = order;
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageList = messageList;
        this.messageType = messageType;
        this.time = time;
    }

    public static MessageListResponse fromChatMessageEntity(ChatMessage chatMessage, Integer order) {
        return MessageListResponse.builder()
                ._id(chatMessage.get_id())
                .order(order)
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
