package seoultech.capstone.menjil.domain.chatbot.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 인자 없는 기본 생성자 필요
@Table(name = "chat_room")
public class Room implements Persistable<String> {

    @Id
    @Column(name = "room_id", length = 255)
    private String id; // Message 의 roomId 와 동일하게.

    @Column(name = "mentee_nickname", nullable = false, length = 50)
    private String menteeNickname;
    @Column(name = "mentor_nickname", nullable = false, length = 100)
    private String mentorNickname;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @Builder
    private Room(String roomId, String menteeNickname, String mentorNickname) {
        this.id = roomId;
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
    }

    /* 생성 날짜가 없으면 새로운 Entity 임을 판단 */
    @Override
    public boolean isNew() {
        return createdDate == null;
    }
}
