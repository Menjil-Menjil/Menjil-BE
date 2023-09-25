package seoultech.capstone.menjil.domain.following.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.following.dto.FollowingUserInfo;
import seoultech.capstone.menjil.domain.following.dto.response.FollowingMentorResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowingService {

    private final AwsS3Handler awsS3Handler;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final QaListRepository qaListRepository;

    private final int AWS_URL_DURATION = 7;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    public Page<FollowingMentorResponse> getFollowMentorsOfUser(String nickname, Pageable pageable) {

        // get follows
        Page<Follow> page = followRepository.findFollowsByUserNickname(nickname, pageable);
        Page<FollowingMentorResponse> followMentorInfoResponse = page.map(follow -> {
            String followNickname = follow.getFollowNickname();

            // 1. get follows
            User user = userRepository.findUserByNickname(followNickname)
                    .orElseThrow(() -> new CustomException(ErrorCode.SERVER_ERROR));
            FollowingUserInfo followingUserInfo = FollowingUserInfo.fromUserEntity(user);

            // set AWS S3 presigned url
            followingUserInfo.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                    BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

            // 2. get last answered messages
            List<String> lastAnsweredMessages = getLastAnsweredMessages(followNickname);

            // 3. get followers count
            Long followersCount = followRepository.countByFollowNickname(followNickname);

            // 4. get answers count
            Long answersCount = qaListRepository.countByMentorNicknameAndAnswerIsNotNull(followNickname);

            return FollowingMentorResponse.of(followingUserInfo, lastAnsweredMessages, followersCount, answersCount);

        });
        return followMentorInfoResponse;
    }

    private List<String> getLastAnsweredMessages(String mentorNickname) {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "question_time", "id")); // Get only the first 2 documents and sort them by 'question_time' and 'id' in descending order
        List<QaList> qaLists = qaListRepository.findAnsweredQuestionsByMentor(mentorNickname, pageable);

        if (qaLists.isEmpty()) {
            return new ArrayList<>();
        } else if (qaLists.size() == 1) {
            return Stream.of(qaLists.get(0))
                    .map(QaList::getQuestionSummary)
                    .collect(Collectors.toList());
        }
        return List.of(qaLists.get(0).getQuestionSummary(), qaLists.get(1).getQuestionSummary());
    }
}
