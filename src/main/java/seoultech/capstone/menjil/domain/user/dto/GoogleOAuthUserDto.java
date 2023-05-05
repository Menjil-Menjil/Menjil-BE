package seoultech.capstone.menjil.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleOAuthUserDto {
    private String id;  // id 값의 범위가 long 이상으로 넓어서, String type 으로 선언
    private String email;
    //    private Boolean verified_email;
    private String name;
//    private String given_name;
//    private String family_name;
//    private String picture;
//    private String locale;


    @Override
    public String toString() {
        return "GoogleOAuthUserDto{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
