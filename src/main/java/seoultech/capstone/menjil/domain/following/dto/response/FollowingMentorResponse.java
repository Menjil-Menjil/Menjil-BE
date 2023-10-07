package seoultech.capstone.menjil.domain.following.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.following.dto.FollowingUserDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingMentorResponse {

    private FollowingUserDto followingUserDto;
    private List<String> lastAnsweredMessages; // 가장 최근에 답변한 질문(최대 2개)
    private Long followersCount;
    private Long answersCount;

    public static FollowingMentorResponse of(FollowingUserDto userInfo, List<String> lastAnsweredMessages,
                                             Long followersCount, Long answersCount) {
        return new FollowingMentorResponse(userInfo, lastAnsweredMessages, followersCount, answersCount);
    }
}
