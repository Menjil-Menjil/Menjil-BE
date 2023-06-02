package seoultech.capstone.menjil.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleOAuthUserDto implements OAuthUserDto {
    private String id;  // id 값의 범위가 long 이상으로 넓어서, String type 으로 선언
    private String email;
    //    private Boolean verifiedEmail;
    private String name;
//    private String givenName;
//    private String familyName;
//    private String picture;
//    private String locale;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String toString() {
        return "GoogleOAuthUserDto{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
