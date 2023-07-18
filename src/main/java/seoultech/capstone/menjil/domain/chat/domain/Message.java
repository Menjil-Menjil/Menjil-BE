package seoultech.capstone.menjil.domain.chat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
public class Message {

//    @Transient
//    public static final String SEQUENCE_NAME = "chat_message_sequence";

    @Id
    private String _id;

    // private Long seq;   // Auto Increment 를 위해 사용

    @Field("room_id")
    private String roomId;

    @Field("sender_nickname")
    private String senderNickname;

    @Field("message")
    private String message;

    @Field("message_type")
    private MessageType messageType;

    @Field("time")
    private LocalDateTime time;

    public void setWelcomeMessage(String roomId, String senderNickname,
                                  String message, MessageType messageType, LocalDateTime time) {
        this.roomId = roomId;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }
}
