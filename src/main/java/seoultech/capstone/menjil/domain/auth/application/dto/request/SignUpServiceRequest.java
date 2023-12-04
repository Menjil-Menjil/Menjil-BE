package seoultech.capstone.menjil.domain.auth.application.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

import javax.validation.constraints.*;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class SignUpServiceRequest {

    @NotBlank
    private String userId;
    @NotBlank
    private String email;
    @NotBlank
    private String provider;    // google, kakao

    /**
     * 사용자에게 입력받는 필수 정보
     */
    @NotBlank
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 공백이나 특수문자가 들어갈 수 없습니다")
    private String nickname;
    @NotNull(message = "생년은 YYYY 형식으로 입력해주세요")
    private Integer birthYear;
    @NotNull(message = "1~12 사이의 값을 입력해주세요")
    private Integer birthMonth;
    @NotBlank
    private String school;
    @NotNull(message = "학점은 0이상 4이하로 작성해주세요")
    @Min(value = 0, message = "학점은 0보다 작을 수 없습니다")
    @Max(value = 4, message = "학점은 4보다 클 수 없습니다")
    private Integer score;  // 학점. 0, 1, 2, 3, 4
    @NotBlank
    private String scoreRange;  // 초반, 중반, 후반; 학점과 연관됨
    @NotNull(message = "졸업 년도는 정수를 입력해주세요")
    private Integer graduateDate;
    @NotNull(message = "졸업 월은 정수를 입력해주세요")
    private Integer graduateMonth;

    @NotBlank
    private String major;
    private String subMajor;
    private String minor;
    private String company;
    private Integer companyYear;

    @NotBlank
    private String field;
    @NotBlank
    private String techStack;

    /**
     * 사용자에게 입력받는 선택 정보
     */
    private String career;
    private String certificate;
    private String awards;
    private String activity;

    @Builder
    private SignUpServiceRequest(String userId, String email, String provider,
                                 String nickname, Integer birthYear, Integer birthMonth,
                                 String school, Integer score, String scoreRange,
                                 Integer graduateDate, Integer graduateMonth, String major,
                                 String subMajor, String minor, String company,
                                 Integer companyYear, String field, String techStack,
                                 String career, String certificate, String awards,
                                 String activity) {
        this.userId = userId;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
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
        this.companyYear = companyYear;
        this.field = field;
        this.techStack = techStack;
        this.career = career;
        this.certificate = certificate;
        this.awards = awards;
        this.activity = activity;
    }

    public User toUserEntity() {
        return User.builder()
                .id(userId)
                .email(email)
                .provider(provider)
                .nickname(nickname)
                .birthYear(birthYear)
                .birthMonth(birthMonth)
                .school(school)
                .score(score)
                .scoreRange(scoreRange)
                .graduateDate(graduateDate)
                .graduateMonth(graduateMonth)
                .major(major)
                .subMajor(subMajor)
                .minor(minor)
                .company(company)
                .companyYear(companyYear)
                .field(field)
                .techStack(techStack)
                .career(career)
                .certificate(certificate)
                .awards(awards)
                .activity(activity)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignUpServiceRequest that = (SignUpServiceRequest) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(email, that.email)
                && Objects.equals(provider, that.provider)
                && Objects.equals(nickname, that.nickname)
                && Objects.equals(birthYear, that.birthYear)
                && Objects.equals(birthMonth, that.birthMonth)
                && Objects.equals(school, that.school)
                && Objects.equals(score, that.score)
                && Objects.equals(scoreRange, that.scoreRange)
                && Objects.equals(graduateDate, that.graduateDate)
                && Objects.equals(graduateMonth, that.graduateMonth)
                && Objects.equals(major, that.major)
                && Objects.equals(subMajor, that.subMajor)
                && Objects.equals(minor, that.minor)
                && Objects.equals(company, that.company)
                && Objects.equals(companyYear, that.companyYear)
                && Objects.equals(field, that.field)
                && Objects.equals(techStack, that.techStack)
                && Objects.equals(career, that.career)
                && Objects.equals(certificate, that.certificate)
                && Objects.equals(awards, that.awards)
                && Objects.equals(activity, that.activity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, provider, nickname,
                birthYear, birthMonth, school, score,
                scoreRange, graduateDate, graduateMonth, major,
                subMajor, minor, company, companyYear,
                field, techStack, career, certificate, awards, activity);
    }
}
