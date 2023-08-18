package seoultech.capstone.menjil.domain.main.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfo {

    private String nickname;
    private String school;
    private String major;
    private String imgUrl;

    @Builder
    private UserInfo(String nickname, String school, String major, String imgUrl) {
        this.nickname = nickname;
        this.school = school;
        this.major = major;
        this.imgUrl = imgUrl;
    }
}
