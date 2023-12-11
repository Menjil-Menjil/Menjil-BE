package seoultech.capstone.menjil.domain.follow.application.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class FollowCreateServiceRequest {

    @NotBlank
    private String userNickname;

    @NotBlank
    private String followNickname;

    public static FollowCreateServiceRequest of(String userNickname, String followNickname) {
        return new FollowCreateServiceRequest(userNickname, followNickname);
    }

    @Builder
    private FollowCreateServiceRequest(String userNickname, String followNickname) {
        this.userNickname = userNickname;
        this.followNickname = followNickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowCreateServiceRequest that = (FollowCreateServiceRequest) o;
        return Objects.equals(userNickname, that.userNickname)
                && Objects.equals(followNickname, that.followNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userNickname, followNickname);
    }
}
