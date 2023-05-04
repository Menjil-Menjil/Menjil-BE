package seoultech.capstone.menzil.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KaKaoOAuthTokenDto {
    /**
     * 카카오 서버로부터 받아올 access token 을 포함한 data
     * response 응답에 맞도록 변수명을 지정해야 한다(camelCase 사용 X)
     */
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private int refresh_token_expires_in;
    private String scope;
    private String token_type;

}
