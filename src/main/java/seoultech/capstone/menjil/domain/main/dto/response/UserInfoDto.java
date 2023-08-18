package seoultech.capstone.menjil.domain.main.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoDto {

    private String nickname;
    private String school;
    private String major;
    private String imgUrl;

    @Builder
    private UserInfoDto(String nickname, String school, String major, String imgUrl) {
        this.nickname = nickname;
        this.school = school;
        this.major = major;
        this.imgUrl = imgUrl;
    }
}
