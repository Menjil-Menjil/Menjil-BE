package seoultech.capstone.menjil.domain.following.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chatbot.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.QaList;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingQaDto;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class FollowingServiceTest {

    @Autowired
    private FollowingService followingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private QaListRepository qaListRepository;

    private final String USER_NICKNAME = "test_user_1";
    private final String FOLLOWER_NICKNAME = "test_follower_1";
    private final String FOLLOWER_NICKNAME_FORMAT = "test_follower_";
    private final int QA_NUM_WITH_ANSWERS = 11;

    @BeforeEach
    void setUp() {
        // Save Users
        User user = createTestUser("google_1112223344", "userA@google.com", USER_NICKNAME);
        userRepository.save(user);

        int followerNum = 30;
        List<User> users = IntStream.rangeClosed(1, followerNum)
                .mapToObj(i -> {
                    String id = "google_" + (1234567899 + i);
                    String email = "testFollower" + i + "@google.com";
                    String nickname = FOLLOWER_NICKNAME_FORMAT + i;
                    return createTestUser(id, email, nickname);
                })
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        // Save Follows
        LocalDateTime now = LocalDateTime.now();
        int followNum = 6;
        List<Follow> follows = IntStream.rangeClosed(1, followNum)
                .mapToObj(i -> {
                    String userNickname = USER_NICKNAME;
                    String followerNickname = FOLLOWER_NICKNAME_FORMAT + i;
                    return Follow.of(userNickname, followerNickname, now.plusMinutes(i));
                })
                .collect(Collectors.toList());
        followRepository.saveAll(follows);

        // Save QaList with answer is not null
        // mentor is only TEST_FOLLOW_NICKNAME_1
        List<QaList> qaLists = IntStream.rangeClosed(1, QA_NUM_WITH_ANSWERS)
                .mapToObj(i -> createTestQaListAndAnswerIsNotNull(FOLLOWER_NICKNAME, i))
                .collect(Collectors.toList());
        qaListRepository.saveAll(qaLists);

        // Save QaList with answer is null
        // mentor is only TEST_MENTOR_NICKNAME_1
        int qaNumWithAnswerIsNull = 8;
        List<QaList> qaListsWithAnswerIsNull = IntStream.rangeClosed(1, qaNumWithAnswerIsNull)
                .mapToObj(i -> createTestQaListAndAnswerIsNull(FOLLOWER_NICKNAME, i))
                .collect(Collectors.toList());
        qaListRepository.saveAll(qaListsWithAnswerIsNull);
    }

    @AfterEach
    void tearDown() {
        // delete mongodb manually
        qaListRepository.deleteAll();
    }

    /**
     * getAllFollowOfUsers
     */
    @Test
    void getAllFollowOfUsers() {
    }

    /**
     * getFollowUserInfo
     */
    @Test
    @DisplayName("case 1: 팔로우 된 사용자가 존재하고, 질문답변 데이터도 존재하는 경우")
    void getFollowUserInfo() {
        // given
        String follower = FOLLOWER_NICKNAME;

        // when
        FollowingUserInfoResponse followMentorInfo = followingService.getFollowUserInfo(follower);

        // then
        assertThat(followMentorInfo).isNotNull();
        assertThat(followMentorInfo.getFollowingUserInfoDto()
                .getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(followMentorInfo.getFollowingUserInfoDto()
                .getCareer()).isNull();
        assertThat(followMentorInfo.getAnswersCount()).isEqualTo(QA_NUM_WITH_ANSWERS);
        assertThat(followMentorInfo.getAnswers().size()).isEqualTo(QA_NUM_WITH_ANSWERS);

        // check if answer_time Order by ASC
        List<FollowingQaDto> qaDtos = followMentorInfo.getAnswers();
        boolean areMessagesInOrder = IntStream.range(0, qaDtos.size() - 1)
                .allMatch(i -> {
                    LocalDateTime currentTime = qaDtos.get(i).getAnswerTime();
                    LocalDateTime nextTime = qaDtos.get(i + 1).getAnswerTime();
                    System.out.println("currentTime.isBefore(nextTime) = " + currentTime.isBefore(nextTime));
                    return currentTime.isBefore(nextTime);
                });
        assertThat(areMessagesInOrder).isTrue();

        /*for (int i = 0; i < qaDtos.size() - 1; i++) {
            LocalDateTime currentTime = qaDtos.get(i).getAnswerTime();
            LocalDateTime nextTime = qaDtos.get(i + 1).getAnswerTime();

            // Assert that the current message's time is before the next message's time
            assertThat(currentTime.isBefore(nextTime)).isTrue();
        }*/
    }

    @Test
    @DisplayName("case 2: 팔로우 된 사용자는 존재하지만, 질문답변 데이터가 존재하지 않는 경우")
    void getFollowUserInfo_QaData_is_Empty() {
        // given
        // test_follower_2의 경우 팔로우는 되어 있으나, 질문답변 정보는 없다.
        String follower_2 = "test_follower_2";

        // when
        FollowingUserInfoResponse followMentorInfo = followingService.getFollowUserInfo(follower_2);

        // then
        assertThat(followMentorInfo).isNotNull();
        assertThat(followMentorInfo.getFollowingUserInfoDto()
                .getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(followMentorInfo.getFollowingUserInfoDto()
                .getCareer()).isNull();

        // here is different with case 1
        assertThat(followMentorInfo.getAnswersCount()).isZero();
        assertThat(followMentorInfo.getAnswers().size()).isZero();
    }

    /**
     이 부분은 회의에서 로직상, 팔로우 관계 없이도 사용자 정보를 열람하도록 정하였으므로, 따로 구현하지 않음.
     */
    /*@Test
    @DisplayName("case 3: 팔로우 관계가 존재하지 않는 사용자에 대해 요청하면, CustomException을 유발한다")
    void getFollowUserInfo_Follow_is_not_existed_between_users() {
        // given
        String follower_100 = "test_follower_100";

        // save User
        User user = createTestUser("google_1112223344", follower_100 + "@google.com", follower_100);
        userRepository.save(user);

        // when // then
        Assertions.assertThatThrownBy(() -> followingService.getFollowUserInfo(follower_100))
                .isInstanceOf(CustomException.class);
    }*/


    /**
     * getLastAnsweredMessages
     */
    @Test
    @DisplayName("case 1: 멘토의 질문 답변 데이터가 0개인 경우(존재하지 않는 경우)")
    void getLastAnsweredMessages_returns_empty_List() {
        // given
        String id = "google_1234123124";
        String email = "mentor2@mentor.com";
        String nickname = "mentor_test_33";
        User mentor1 = createTestUser(id, email, nickname);

        // when
        List<String> lastAnsweredMessages = followingService.getLastAnsweredMessages(mentor1.getNickname());

        // then
        assertThat(lastAnsweredMessages).isEmpty();
    }

    @Test
    @DisplayName("case 2: 멘토의 질문 답변 데이터가 1개인 경우")
    void getLastAnsweredMessages_size_is_one() {
        // given
        String id = "google_1234123124";
        String email = "mentor2@mentor.com";
        String nickname = "mentor_test_33";
        User mentor1 = createTestUser(id, email, nickname);

        int qaNum = 1;
        List<QaList> qaLists = IntStream.rangeClosed(1, qaNum)
                .mapToObj(i -> createTestQaListAndAnswerIsNotNull(mentor1.getNickname(), i))
                .collect(Collectors.toList());
        qaListRepository.saveAll(qaLists);

        // when
        List<String> lastAnsweredMessages = followingService.getLastAnsweredMessages(mentor1.getNickname());

        // then
        assertThat(lastAnsweredMessages.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("case 3: 멘토의 질문 답변 데이터가 2개 이상인 경우")
    void getLastAnsweredMessages_size_is_more_than_two() {
        // given
        String id = "google_1234123124";
        String email = "mentor2@mentor.com";
        String nickname = "mentor_test_33";
        User mentor1 = createTestUser(id, email, nickname);
        int qaNum = 5;
        List<QaList> qaLists = IntStream.rangeClosed(1, qaNum)
                .mapToObj(i -> createTestQaListAndAnswerIsNotNull(mentor1.getNickname(), i))
                .collect(Collectors.toList());
        qaListRepository.saveAll(qaLists);

        // when
        List<String> lastAnsweredMessages = followingService.getLastAnsweredMessages(mentor1.getNickname());

        // then
        assertThat(lastAnsweredMessages.size()).isEqualTo(2);
    }

    private User createTestUser(String id, String email, String nickname) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("컴퓨터공학과").subMajor("심리학과")
                .minor(null).field("백엔드").techStack("AWS")
                .company(null)
                .companyYear(0)
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .imgUrl("default/profile.png")  // set img url
                .build();
    }

    private QaList createTestQaListAndAnswerIsNotNull(String mentorNickname, int index) {
        LocalDateTime now = LocalDateTime.now();
        return QaList.builder()
                .menteeNickname(USER_NICKNAME)
                .mentorNickname(mentorNickname)
                .questionOrigin("origin message_" + index)
                .questionSummary("summary message_" + index)
                .questionSummaryEn("summary eng message_" + index)
                .questionTime(now.minusHours(index))
                .answer("answer message_" + index)
                .answerTime(now.plusMinutes(index))
                .build();
    }

    // TODO: QaList를 QaObject로 명칭 변경, 및 menteeNickname, mentorNickname column 명 변경
    private QaList createTestQaListAndAnswerIsNull(String mentorNickname, int index) {
        LocalDateTime now = LocalDateTime.now();
        return QaList.builder()
                .menteeNickname(USER_NICKNAME)
                .mentorNickname(mentorNickname)
                .questionOrigin("origin message_" + index)
                .questionSummary("summary message_" + index)
                .questionSummaryEn("summary eng message_" + index)
                .questionTime(now.minusHours(index))
                .answer(null)       // here is null
                .answerTime(null)   // here is null
                .build();
    }
}