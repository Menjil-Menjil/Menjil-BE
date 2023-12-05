package seoultech.capstone.menjil.domain.following.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingUserResponse {

    private FollowingUserDto followingUserDto;
    private List<String> lastAnsweredMessages; // 가장 최근에 답변한 질문(최대 2개)
    private Long followersCount;
    private Long answersCount;

    public static FollowingUserResponse of(FollowingUserDto userInfo, List<String> lastAnsweredMessages,
                                           Long followersCount, Long answersCount) {
        return new FollowingUserResponse(userInfo, lastAnsweredMessages, followersCount, answersCount);
    }
}
