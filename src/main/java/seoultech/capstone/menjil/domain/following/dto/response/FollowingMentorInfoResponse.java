package seoultech.capstone.menjil.domain.following.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.following.dto.FollowingQaDto;
import seoultech.capstone.menjil.domain.following.dto.FollowingUserInfoDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingMentorInfoResponse {

    private FollowingUserInfoDto followingUserInfoDto;
    private Long answersCount;
    private List<FollowingQaDto> answers;

    public static FollowingMentorInfoResponse of(FollowingUserInfoDto userInfo,
                                                 Long answersCount, List<FollowingQaDto> answers) {
        return new FollowingMentorInfoResponse(userInfo, answersCount, answers);
    }
}

