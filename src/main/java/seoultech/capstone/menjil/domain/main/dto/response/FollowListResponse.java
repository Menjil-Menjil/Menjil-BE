package seoultech.capstone.menjil.domain.main.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowListResponse {
    private String nickname;
    private String company;     // 재직 중인 회사
    private String techStack;
    private String imgUrl;

    public static FollowListResponse fromUserEntity(User user) {
        return new FollowListResponse(user.getNickname(), user.getCompany(),
                user.getTechStack(), user.getImgUrl());
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
