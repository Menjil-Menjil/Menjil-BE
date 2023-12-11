package seoultech.capstone.menjil.domain.auth.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignInServiceRequest;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {
    private String email;
    private String provider;

    public SignInServiceRequest toServiceRequest() {
        return SignInServiceRequest.builder()
                .email(email)
                .provider(provider)
                .build();
    }

}
