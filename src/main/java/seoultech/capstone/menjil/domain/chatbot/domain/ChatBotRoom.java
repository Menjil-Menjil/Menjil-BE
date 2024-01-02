package seoultech.capstone.menjil.domain.chatbot.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 인자 없는 기본 생성자 필요
@Table(name = "chatbot_room")
public class ChatBotRoom {

    @Id
    @Column(name = "room_id", length = 255)
    private String roomId; // Message 의 roomId 와 동일하게.

    @Column(name = "initiator_nickname", nullable = false, length = 100)
    private String initiatorNickname;

    @Column(name = "recipient_nickname", nullable = false, length = 100)
    private String recipientNickname;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @Builder
    private ChatBotRoom(String roomId, String initiatorNickname, String recipientNickname) {
        this.roomId = roomId;
        this.initiatorNickname = initiatorNickname;
        this.recipientNickname = recipientNickname;
    }
}
