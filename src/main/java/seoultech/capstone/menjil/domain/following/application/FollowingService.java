package seoultech.capstone.menjil.domain.following.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chatbot.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.QaList;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingQaDto;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserDto;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserInfoDto;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserInfoResponse;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public Page<FollowingUserResponse> getAllFollowOfUsers(String nickname, Pageable pageable) {

        // get follows
        // TODO: 여기도 User랑 Follow 사이에 연관관계 설정해야 하는 거 아닌가?
        Page<Follow> page = followRepository.findFollowsByUserNickname(nickname, pageable);
        Page<FollowingUserResponse> followMentorInfoResponse = page.map(follow -> {
            String followNickname = follow.getFollowNickname();

            // 1. get follows
            User user = userRepository.findUserByNickname(followNickname)
                    .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
            FollowingUserDto followingUserDto = FollowingUserDto.fromUserEntity(user);

            // set AWS S3 presigned url
            followingUserDto.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                    BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

            // 2. get last answered messages
            List<String> lastAnsweredMessages = getLastAnsweredMessages(followNickname);

            // 3. get followers count
            // TODO: user와 follow를 join하면 쿼리를 두 번 날리지 않아도 될 것으로 생각됨. 추후 JPA 공부한 뒤 적용해볼 것
            Long followersCount = followRepository.countByFollowNickname(followNickname);

            // 4. get answers count
            Long answersCount = qaListRepository.countByMentorNicknameAndAnswerIsNotNull(followNickname);

            return FollowingUserResponse.of(followingUserDto, lastAnsweredMessages, followersCount, answersCount);

        });
        return followMentorInfoResponse;
    }

    @Transactional
    public FollowingUserInfoResponse getFollowUserInfo(String followNickname) {
        // Exception 1: 사용자가 존재하지 않는 경우
        User user = userRepository.findUserByNickname(followNickname)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        // Exception 2: 팔로우 관계가 존재하지 않는 경우(고려하지 않기로 프론트와 협의됨)

        // 1. 사용자 정보
        FollowingUserInfoDto followingUserInfoDto = FollowingUserInfoDto.fromUserEntity(user);
        followingUserInfoDto.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

        // 2. 작성 답변 개수
        Long answersCount = qaListRepository.countByMentorNicknameAndAnswerIsNotNull(followNickname);

        // 3. 작성 질문/답변 목록 리스트
        // TODO: 조회수, 좋아요 추가
        Sort sort = Sort.by(Sort.Order.asc("answer_time"));
        List<FollowingQaDto> followingQaDtos = qaListRepository.findQuestionAndAnswerWithMentorNickname(followNickname, sort)
                .stream()
                .map(q -> new FollowingQaDto(q.getQuestionOrigin(), q.getQuestionSummary(),
                        q.getAnswer(), q.getAnswerTime(), q.getViews(), q.getLikes()))
                .collect(Collectors.toList());

        return FollowingUserInfoResponse.of(followingUserInfoDto, answersCount, followingQaDtos);
    }

    protected List<String> getLastAnsweredMessages(String mentorNickname) {
        int page = 0;
        int size = 2;
        // Get only the first 2 documents and sort them by 'question_time' and 'id' in descending order
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "question_time", "id"));
        List<QaList> qaLists = qaListRepository.findAnsweredQuestionsByMentor(mentorNickname, pageable);

        /*
         At first, I checked if qaLists is empty(), or size == 1, or else. But
         The stream() and map() method calls will produce an empty list if qaLists is empty,
         and will otherwise produce a list of question summaries,
         making the explicit checks for qaLists.isEmpty() and qaLists.size() == 1 unnecessary.
         */
        return qaLists.stream()
                .map(QaList::getQuestionSummary)
                .collect(Collectors.toList());
    }
}
