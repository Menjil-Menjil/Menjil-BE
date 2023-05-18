package seoultech.capstone.menjil.domain.user.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor  // 기본 생성자 필수
@Embeddable
public class OptionInfo {

    @Column(columnDefinition = "TEXT")
    private String career;

    @Column(columnDefinition = "TEXT")
    private String certificate;

    @Column(columnDefinition = "TEXT")
    private String awards;

    @Column(columnDefinition = "TEXT")
    private String activity;

    public OptionInfo(String career, String certificate, String awards, String activity) {
        this.career = career;
        this.certificate = certificate;
        this.awards = awards;
        this.activity = activity;
    }
}
