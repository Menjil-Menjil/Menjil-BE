package seoultech.capstone.menjil.domain.main.application;

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
import seoultech.capstone.menjil.domain.main.dto.response.FollowUserResponse;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainPageService {

    private final AwsS3Handler awsS3Handler;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final QaListRepository qaListRepository;

    private final int AWS_URL_DURATION = 7;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * nickname은, 추후 멘토 추천 알고리즘 사용시 필요할 수 있으므로, 우선 받도록 하였으나.
     * 현재 수행하는 기능은 없다.
     */
    public Page<MentorInfoResponse> getMentors(String nickname, Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        Page<MentorInfoResponse> mentorInfoResponse = page.map(user -> {
            MentorInfoResponse dto = MentorInfoResponse.fromUserEntity(user);

            // set AWS S3 presigned url
            dto.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                    BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

            // set lastAnsweredMessage
            dto.setLastAnsweredMessage(getLastAnsweredMessages(user.getNickname()));
            return dto;
        });
        return mentorInfoResponse;
    }

    public List<FollowUserResponse> getFollowersOfUser(String nickname) {
        // TODO: 이 부분도 join을 사용하거나 혹은 User & Follow 엔티티 사이에 연관 관계를 설정한다면, query 성능을 조금 더 최적화가 가능할 것으로 보임.
        List<User> followUsers = followRepository.findFollowsByUserNicknameOrderByCreatedDateAsc(nickname)
                .stream()
                .map(Follow::getFollowNickname)
                .map(userRepository::findUserByNickname)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // 팔로우를 가장 최근에 한 사용자, 즉 createdDate가 가장 나중인 사용자가 리스트 인덱스 0에 오도록 변경
        Collections.reverse(followUsers);

        return followUsers.stream()
                .map(user -> {
                    FollowUserResponse response = FollowUserResponse.fromUserEntity(user);
                    response.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                            BUCKET_NAME, response.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));
                    return response;
                })
                .collect(Collectors.toList());
    }

    protected List<String> getLastAnsweredMessages(String mentorNickname) {
        int page = 0;
        int size = 2;
        // Get only the first 2 documents and sort them by 'question_time' and 'id' in descending order
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "question_time", "id"));
        List<QaList> qaLists = qaListRepository.findAnsweredQuestionsByMentor(mentorNickname, pageable);

        /*
         At first, checked if qaLists is empty(), or size == 1, or else.
         But the stream() and map() method calls will produce an empty list if qaLists is empty,
         and will otherwise produce a list of question summaries,
         making the explicit checks for qaLists.isEmpty() and qaLists.size() == 1 unnecessary.
         */
        return qaLists.stream()
                .map(QaList::getQuestionSummary)
                .collect(Collectors.toList());
    }
}
