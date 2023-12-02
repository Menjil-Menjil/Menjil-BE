package seoultech.capstone.menjil.domain.follow.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.follow.application.dto.request.FollowCreateServiceRequest;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowCreateRequest {

    @NotBlank
    private String userNickname;

    @NotBlank
    private String followNickname;

    public static FollowCreateRequest of(String userNickname, String followNickname) {
        return new FollowCreateRequest(userNickname, followNickname);
    }

    public FollowCreateServiceRequest toServiceRequest() {
        return FollowCreateServiceRequest.builder()
                .userNickname(userNickname)
                .followNickname(followNickname)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowCreateRequest that = (FollowCreateRequest) o;
        return Objects.equals(userNickname, that.userNickname)
                && Objects.equals(followNickname, that.followNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userNickname, followNickname);
    }
}
