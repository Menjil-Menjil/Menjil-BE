package seoultech.capstone.menjil.domain.chat.domain;

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
@Table(name = "chat_room")
public class Room {

    @Id
    @Column(name = "room_id", unique = true, nullable = false, length = 255)
    private String roomId; // Message 의 roomId 와 동일하게.

    @Column(name = "mentee_nickname", nullable = false, length = 50)
    private String menteeNickname;
    @Column(name = "mentor_nickname", nullable = false, length = 100)
    private String mentorNickname;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdDate;

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Builder
    public Room(String roomId, String menteeNickname, String mentorNickname) {
        this.roomId = roomId;
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
    }


}
