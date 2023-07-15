package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignInResponseDto {
    private int code;
    private String accessToken;
    private String refreshToken;
}
