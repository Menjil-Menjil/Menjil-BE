package seoultech.capstone.menjil.domain.user.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
// When JPA Entity class extends BaseTimeEntity class, files(createdDate, etc) are recognized as column
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedDate;

}
