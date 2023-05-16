package seoultech.capstone.menjil.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.user.domain.UserRole;
import seoultech.capstone.menjil.domain.user.dto.UserDto;

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
    private String data;

    /**
     * 사용자에게 입력받는 필수 정보
     */
    @NotBlank
    private String nickname;
    @NotNull
    private UserRole role;
    @NotNull
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[012])$")
    private String birthDate;
    @NotBlank
    private String school;
    @NotNull
    @Min(0)
    @Max(4)
    private Integer score;  // 학점. 0, 1, 2, 3, 4
    @NotBlank
    private String scoreRange;  // 초반, 중반, 후반; 학점과 연관됨
    @NotNull
    private Integer graduateDate;
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
                .birthDate(birthDate)
                .school(school)
                .score(score)
                .scoreRange(scoreRange)
                .graduateDate(graduateDate)
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
