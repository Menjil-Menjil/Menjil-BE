package seoultech.capstone.menjil.domain.following.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}
