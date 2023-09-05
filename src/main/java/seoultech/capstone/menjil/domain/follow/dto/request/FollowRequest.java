package seoultech.capstone.menjil.domain.follow.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {

    @NotBlank
    private String userNickname;

    @NotBlank
    private String followNickname;
}
