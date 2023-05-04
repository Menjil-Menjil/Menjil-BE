package seoultech.capstone.menzil.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)   // JSON type(snake case) -> java(camel case)
public class KaKaoOAuthUserDto {

    private String id;  // Google id 와 동일한 Primitive type 사용
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)   // JSON type(snake case) -> java(camel case)
    public static class KakaoAccount {
        private Profile profile;
        private String email;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
        }
    }

    @Override
    public String toString() {
        return "KaKaoOAuthUserDto{" +
                "id='" + id + '\'' +
                ", kakaoAccount.email =" + kakaoAccount.getEmail() +
                ", kakaoAccount.profile.nickname =" + kakaoAccount.getProfile().getNickname() +
                '}';
    }
}
