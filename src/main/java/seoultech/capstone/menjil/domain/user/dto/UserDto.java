package seoultech.capstone.menjil.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import seoultech.capstone.menjil.domain.user.domain.OptionInfo;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.domain.user.domain.UserRole;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class UserDto {

    /**
     * 유저 정보: decode by Jwt
     */
    @NotBlank
    private String id;
    @NotBlank
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    private String provider;

    /**
     * 사용자에게 입력받는 필수 정보
     */
    @NotBlank
    private String nickname;
    @NotBlank
    private UserRole role;
    @NotNull
    private Integer birthYear;
    @NotNull
    private Integer birthMonth;
    @NotBlank
    private String school;
    @NotBlank
    @Min(0)
    @Max(4)
    private Integer score;  // 학점. 0, 1, 2, 3, 4
    @NotBlank
    private String scoreRange;  // 초반, 중반, 후반; 학점과 연관됨
    @NotNull
    private Integer graduateDate;
    @NotNull
    private Integer graduateMonth;
    @NotBlank
    private String major;
    private String subMajor;
    private String minor;
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
    public UserDto(String id, String email, String name, String provider, String nickname,
                   UserRole role, Integer birthYear, Integer birthMonth, String school,
                   Integer score, String scoreRange, Integer graduateDate, Integer graduateMonth, String major,
                   String subMajor, String minor, String field, String techStack,
                   String career, String certificate, String awards, String activity) {
        this.id = id;
        this.email = email;
        this.name = name;
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
        this.field = field;
        this.techStack = techStack;
        this.career = career;
        this.certificate = certificate;
        this.awards = awards;
        this.activity = activity;
    }

    public User toEntity() {
        return User.builder()
                .id(id)
                .email(email)
                .name(name)
                .provider(provider)
                .nickname(nickname)
                .role(role)
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
                .field(field)
                .techStack(techStack)
                .optionInfo(new OptionInfo(career, certificate, awards, activity))  // use Embedded type
                .build();
    }


}
