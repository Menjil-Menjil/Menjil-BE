package seoultech.capstone.menjil.domain.chatbot.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
public class ChatMessage {

    @Id
    private String _id;

    @Field("room_id")
    private String roomId;

    @Field("sender_type")
    private SenderType senderType;

    @Field("sender_nickname")
    private String senderNickname;

    @Field(value = "message", write = Field.Write.ALWAYS)   // To persist field value can be null
    private String message;

    @Field(value = "message_list", write = Field.Write.ALWAYS)  // To persist field value can be null
    private Object messageList;

    @Field("message_type")
    private MessageType messageType;

    @Field("time")
    private LocalDateTime time;

    public void setWelcomeMessage(String roomId, SenderType senderType, String senderNickname,
                                  String message, Object messageList,
                                  MessageType messageType, LocalDateTime time) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageList = messageList;
        this.messageType = messageType;
        this.time = time;
    }

    public void setLambdaMessage(String message) {
        this.message = message;
    }

    public void setLambdaMessageList(Object messageList) {
        this.messageList = messageList;
    }

    @Builder
    private ChatMessage(String _id, String roomId,
                        SenderType senderType, String senderNickname,
                        String message, Object messageList, MessageType messageType, LocalDateTime time) {
        this._id = _id;
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageList = messageList;
        this.messageType = messageType;
        this.time = time;
    }
}
