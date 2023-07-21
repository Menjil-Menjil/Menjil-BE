package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInResponseDto {
    private String accessToken;
    private String refreshToken;

    @Builder

    public SignInResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
