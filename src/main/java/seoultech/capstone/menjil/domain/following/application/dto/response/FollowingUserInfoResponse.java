package seoultech.capstone.menjil.domain.following.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserInfoDto;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingQaDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingUserInfoResponse {

    private FollowingUserInfoDto followingUserInfoDto;
    private Long answersCount;
    private List<FollowingQaDto> answers;

    public static FollowingUserInfoResponse of(FollowingUserInfoDto userInfo,
                                               Long answersCount, List<FollowingQaDto> answers) {
        return new FollowingUserInfoResponse(userInfo, answersCount, answers);
    }
}

