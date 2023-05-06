package seoultech.capstone.menjil.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GoogleOAuthTokenDto {
    /**
     * 구글 서버로부터 받아올 access token 을 포함한 data
     */
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String idToken;


}
