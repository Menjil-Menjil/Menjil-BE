package seoultech.capstone.menjil.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.OptionInfo;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;

import javax.validation.constraints.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

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
    @NotNull
    private UserRole role;
    @NotNull(message = "생년은 OOOO형식으로 입력해주세요")
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
    public User toUser() {
        return User.builder()
                .id(userId)
                .email(email)
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
