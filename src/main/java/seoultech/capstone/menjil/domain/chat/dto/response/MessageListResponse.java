package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MessageListResponse {

    private String _id; // different from MessageRequest
    private Integer order; // different from MessageRequest
    private String roomId;
    private SenderType senderType;
    private String senderNickname;
    private List<AwsLambdaResponse> messageList;
    private MessageType messageType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime time;

    @Builder
    private MessageListResponse(String _id, Integer order, String roomId,
                                SenderType senderType, String senderNickname,
                                List<AwsLambdaResponse> messageList, MessageType messageType,
                                LocalDateTime time) {
        this._id = _id;
        this.order = order;
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
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
                .messageList((List<AwsLambdaResponse>) chatMessage.getMessageList())
                .messageType(chatMessage.getMessageType())
                .time(chatMessage.getTime())
                .build();
    }


}
