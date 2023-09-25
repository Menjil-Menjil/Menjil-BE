package seoultech.capstone.menjil.domain.following.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingUserInfo {

    private String nickname;
    private String company;     // 재직 중인 회사
    private String field;       // 관심 분야
    private String techStack;   // 기술 스택
    private String school;
    private String major;       // 본전공
    private String imgUrl;

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public static FollowingUserInfo fromUserEntity(User user) {
        return new FollowingUserInfo(user.getNickname(), user.getCompany(), user.getField(),
                user.getTechStack(), user.getSchool(), user.getMajor(), user.getImgUrl());
    }

}
