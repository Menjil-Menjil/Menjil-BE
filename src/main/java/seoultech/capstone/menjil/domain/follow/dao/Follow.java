package seoultech.capstone.menjil.domain.follow.dao;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Table(name = "follows")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_nickname", nullable = false, length = 100)
    private String userNickname;

    @Column(name = "follow_nickname", nullable = false, length = 100)
    private String followNickname;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdDate;

    public Follow(String userNickname, String followNickname, LocalDateTime createdDate) {
        this.userNickname = userNickname;
        this.followNickname = followNickname;
        this.createdDate = createdDate;
    }

    public static Follow of(String userNickname, String followNickname, LocalDateTime createdDate) {
        createdDate = createdDate.withNano(0);    // Ignore milliseconds
        return new Follow(userNickname, followNickname, createdDate);
    }
}
