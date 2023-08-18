package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignInResponseDto {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private String school;
    private String major;
    private String imgUrl;

    @Builder
    private SignInResponseDto(String accessToken, String refreshToken,
                              String nickname, String school, String major, String imgUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.nickname = nickname;
        this.school = school;
        this.major = major;
        this.imgUrl = imgUrl;
    }
}
