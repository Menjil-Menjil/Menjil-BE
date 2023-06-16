package seoultech.capstone.menjil.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.user.domain.UserRole;
import seoultech.capstone.menjil.domain.user.dto.UserDto;
import seoultech.capstone.menjil.domain.user.validation.customAnnotation.Expired;

import javax.validation.constraints.*;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    /**
     * 유저 JWT 정보
     */
    @NotBlank
    @Expired
    private String data;

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

    public UserDto toUserDto(Map<String, Object> dataMap) {
        return UserDto.builder()
                .id(dataMap.get("id").toString())
                .email(dataMap.get("email").toString())
                .name(dataMap.get("name").toString())
                .provider(dataMap.get("provider").toString())
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
                .career(career)
                .certificate(certificate)
                .awards(awards)
                .activity(activity)
                .build();
    }
}
