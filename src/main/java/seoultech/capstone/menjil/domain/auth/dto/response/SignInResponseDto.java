package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInResponseDto {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private String school;
    private String major;
    private String imgUrl;

    public static SignInResponseDto of(String accessToken, String refreshToken,
                                        String nickname, String school, String major, String imgUrl) {
        return new SignInResponseDto(accessToken, refreshToken, nickname, school, major, imgUrl);
    }
}
