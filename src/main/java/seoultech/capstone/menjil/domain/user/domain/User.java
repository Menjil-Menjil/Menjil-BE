package seoultech.capstone.menjil.domain.user.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 인자 없는 기본 생성자 필요
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    /**
     * 이 부분 정보는 기존에 클라이언트에게 보낸 정보를 다시 받아온 뒤, jwt 복호화 과정을 진행하여 얻는다.
     */
    @Id // 식별자 지정
    @Column(name = "provider_id", length = 50)
    private String id;  // ex) google_a123sasf

    @Column(nullable = false, length = 50)
    private String email;   // kakao 의 경우, 구글 이메일로 가입할 수 있으므로 column=unique 는 적합하지 않다.

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String provider; // google, kakao

    /**
     * 아래 부터는 사용자에게 추가적으로 입력받는 정보
     * 여기부터는 필수적으로 입력 받는 정보
     */
    @Column(nullable = false, unique = true)
    private String nickname;    // 고유 유저를 식별할 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;    // Mentor or Mentee

    @Column(name = "birth_date", nullable = false)
    private String birthDate;

    @Column(length = 30, nullable = false)
    private String school;      // 교육 기관

    @Column(nullable = false)
    private Integer score;

    @Column(name = "score_range", nullable = false)
    private String scoreRange;

    @Column(name = "graduate_date", nullable = false)
    private Integer graduateDate; // 졸업 년도

    @Column(length = 20, nullable = false)
    private String major;       // 본전공

    @Column(name = "sub_major", length = 20)
    private String subMajor;    // 복수전공

    @Column(length = 20)
    private String minor;       // 부전공

    @Column(nullable = false)
    private String field;       // 관심 분야

    @Column(name = "tech_stack", nullable = false)
    private String techStack;   // 기술 스택

    /**
     * 아래는 선택 입력 정보: 가입 단계에서 굳이 받지 않아도 되는 정보
     */
    @Embedded
    private OptionInfo optionInfo;
}
