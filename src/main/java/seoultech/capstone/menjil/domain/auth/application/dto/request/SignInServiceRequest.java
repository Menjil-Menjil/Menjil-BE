package seoultech.capstone.menjil.domain.auth.application.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignInServiceRequest {

    private String email;
    private String provider;

    @Builder
    private SignInServiceRequest(String email, String provider) {
        this.email = email;
        this.provider = provider;
    }

}
