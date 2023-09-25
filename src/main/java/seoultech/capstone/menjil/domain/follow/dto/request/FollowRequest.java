package seoultech.capstone.menjil.domain.follow.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {

    @NotBlank
    private String userNickname;

    @NotBlank
    private String followNickname;

    public static FollowRequest of(String userNickname, String followNickname) {
        return new FollowRequest(userNickname, followNickname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowRequest that = (FollowRequest) o;
        return Objects.equals(userNickname, that.userNickname)
                && Objects.equals(followNickname, that.followNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userNickname, followNickname);
    }
}
