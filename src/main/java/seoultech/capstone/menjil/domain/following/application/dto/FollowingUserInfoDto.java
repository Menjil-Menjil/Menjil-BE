package seoultech.capstone.menjil.domain.following.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
@NoArgsConstructor
public class FollowingUserInfoDto {

    private String nickname;
    private String company;     // 재직 중인 회사
    private String field;       // 관심 분야
    private String school;
    private String major;       // 본전공
    private String subMajor;    // 복수전공

    private String minor;       // 부전공
    private String techStack;   // 기술 스택
    private String imgUrl;

    private String career;
    private String certificate;
    private String awards;
    private String activity;


    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public static FollowingUserInfoDto fromUserEntity(User user) {
        return new FollowingUserInfoDto(user.getNickname(), user.getCompany(), user.getField(),
                user.getSchool(), user.getMajor(), user.getSubMajor(), user.getMinor(),
                user.getTechStack(), user.getImgUrl(),
                user.getCareer(), user.getCertificate(),
                user.getAwards(), user.getActivity()
        );
    }

    @Builder
    private FollowingUserInfoDto(String nickname, String company, String field, String school,
                                String major, String subMajor, String minor,
                                String techStack, String imgUrl, String career,
                                String certificate, String awards, String activity) {
        this.nickname = nickname;
        this.company = company;
        this.field = field;
        this.school = school;
        this.major = major;
        this.subMajor = subMajor;
        this.minor = minor;
        this.techStack = techStack;
        this.imgUrl = imgUrl;
        this.career = career;
        this.certificate = certificate;
        this.awards = awards;
        this.activity = activity;
    }
}
