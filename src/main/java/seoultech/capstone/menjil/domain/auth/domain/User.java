package seoultech.capstone.menjil.domain.auth.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 인자 없는 기본 생성자 필요
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name = "UniqueNickName",
        columnNames = {"nickname"})})
public class User extends BaseTimeEntity {

    @Id // 식별자 지정
    @Column(name = "user_id", length = 50)
    private String id;  // ex) google_3214321

    @Column(nullable = false, length = 50)
    private String email;   // kakao 의 경우, 구글 이메일로 가입할 수 있으므로 column=unique 는 적합하지 않다.

    @Column(nullable = false, length = 15)
    private String provider; // google, kakao

    /**
     * 아래 부터는 사용자에게 추가적으로 입력받는 정보
     * 여기부터는 필수적으로 입력 받는 정보
     */
    @Column(nullable = false, length = 100)
    private String nickname;    // 고유 유저를 식별할 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private UserRole role;    // Mentor or Mentee

    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Column(name = "birth_month", nullable = false)
    private Integer birthMonth;

    @Column(nullable = false, length = 30)
    private String school;      // 교육 기관

    @Column(nullable = false)
    private Integer score;

    @Column(name = "score_range", nullable = false, length = 15)
    private String scoreRange;

    @Column(name = "graduate_date", nullable = false)
    private Integer graduateDate; // 졸업 년도

    @Column(name = "graduate_month", nullable = false)
    private Integer graduateMonth; // 졸업 월

    @Column(nullable = false, length = 50)
    private String major;       // 본전공

    @Column(name = "sub_major", length = 50)
    private String subMajor;    // 복수전공

    @Column(length = 50)
    private String minor;       // 부전공

    @Column(length = 100)
    private String company;     // 재직 중인 회사.

    @Column(nullable = false)
    private String field;       // 관심 분야

    @Column(name = "tech_stack", nullable = false)
    private String techStack;   // 기술 스택

    /**
     * 아래는 선택 입력 정보: 가입 단계에서 굳이 받지 않아도 되는 정보
     */
    @Embedded
    private OptionInfo optionInfo;

    @Column(name = "img_url", length = 100)
    private String imgUrl;

    /* Builder 로만 생성할 수 있도록 private 설정 */
    @Builder
    private User(String id, String email, String provider, String nickname,
                 UserRole role, Integer birthYear, Integer birthMonth, String school,
                 Integer score, String scoreRange, Integer graduateDate, Integer graduateMonth, String major,
                 String subMajor, String minor, String company, String field, String techStack,
                 OptionInfo optionInfo, String imgUrl) {
        this.id = id;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
        this.role = role;
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.school = school;
        this.score = score;
        this.scoreRange = scoreRange;
        this.graduateDate = graduateDate;
        this.graduateMonth = graduateMonth;
        this.major = major;
        this.subMajor = subMajor;
        this.minor = minor;
        this.company = company;
        this.field = field;
        this.techStack = techStack;
        this.optionInfo = optionInfo;
        this.imgUrl = imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

