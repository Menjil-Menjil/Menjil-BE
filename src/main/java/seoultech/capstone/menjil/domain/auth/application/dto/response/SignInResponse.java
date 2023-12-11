package seoultech.capstone.menjil.domain.auth.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInResponse {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private String school;
    private String major;
    private String imgUrl;

    public static SignInResponse of(String accessToken, String refreshToken,
                                    String nickname, String school, String major, String imgUrl) {
        return new SignInResponse(accessToken, refreshToken, nickname, school, major, imgUrl);
    }
}
