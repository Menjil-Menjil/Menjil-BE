package seoultech.capstone.menjil.domain.main.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.main.dto.response.FollowUserResponse;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MainPageServiceTest {

    @Autowired
    private MainPageService mainPageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private QaListRepository qaListRepository;

    private final int SIZE = 3;
    private final Sort SORT = Sort.by(
            Sort.Order.asc("createdDate"),
            Sort.Order.asc("nickname")
    );
    private final String TEST_MENTEE_NICKNAME = "test_mentee_nickname";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_nickname";

    @BeforeEach
    void setUp() {
        User userA = createTestUser("google_1231323", "test@google.com", TEST_MENTEE_NICKNAME);
        User userB = createTestUser("google_1231324", "test2@google.com", TEST_MENTOR_NICKNAME);
        userRepository.saveAll(List.of(userA, userB));
    }

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
        qaListRepository.deleteAll();
    }

    /**
     * getMentors
     */
    @Test
    @DisplayName("page=0, sort=3, order by createdDate, nickname DESC, mentor=8: 데이터 3개 리턴")
    void getMentors_page_0() throws InterruptedException {
        // given
        int pageNum = 0;
        PageRequest pageRequest = PageRequest.of(pageNum, SIZE, SORT);

        // save 8 mentor data in users table
        // 헷갈림 방지를 위해, @BeforeEach에서 저장한 데이터 제거
        userRepository.deleteAll();

        int MENTOR_NUM = 8;
        String MENTOR_NICKNAME = "test_mentor_";
        List<User> users = IntStream.rangeClosed(1, MENTOR_NUM)
                .mapToObj(i -> {
                    String testId = "google_" + i;
                    String testEmail = "test_" + i + "@gmail.com";
                    String testNickname = MENTOR_NICKNAME + i;
                    return createTestUser(testId, testEmail, testNickname);
                })
                .collect(Collectors.toList());  // collects the User objects into a List<User>

        // createdDate 값을 조절하기 위해, Thread.sleep() 사용: 하지만 MentorInfoResponse에서 시간 데이터를 사용하지 않으므로, 큰 의미는 없다.
        // 추후 리팩토링할때 무시할 것
        userRepository.saveAll(List.of(users.get(0), users.get(1)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(2), users.get(3), users.get(4)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(5), users.get(6), users.get(7)));

        // when
        // nickname은 중요하지 않다.
        Page<MentorInfoResponse> mentorList = mainPageService.getMentors("test1", pageRequest);

        // then
        assertThat(mentorList.getSize()).isEqualTo(3);

        MentorInfoResponse firstMentor = mentorList.getContent().get(0);
        MentorInfoResponse secondMentor = mentorList.getContent().get(1);
        MentorInfoResponse thirdMentor = mentorList.getContent().get(2);

        assertThat(firstMentor.getNickname()).isEqualTo(MENTOR_NICKNAME + 1);
        assertThat(firstMentor.getImgUrl()).isNotBlank();
        assertThat(secondMentor.getNickname()).isEqualTo(MENTOR_NICKNAME + 2);
        assertThat(thirdMentor.getNickname()).isEqualTo(MENTOR_NICKNAME + 3);
    }

    @Test
    @DisplayName("page=2, sort=3, order by createdDate, nickname DESC, mentor=8 : Content 2개 리턴")
    void getMentors_page_2() throws InterruptedException {
        // given
        int pageNum = 2;
        PageRequest pageRequest = PageRequest.of(pageNum, SIZE, SORT);

        // save 8 mentor data in users table
        // 헷갈림 방지를 위해, @BeforeEach에서 저장한 데이터 제거
        userRepository.deleteAll();

        int MENTOR_NUM = 8;
        String MENTOR_NICKNAME = "test_mentor_";
        List<User> users = IntStream.rangeClosed(1, MENTOR_NUM)
                .mapToObj(i -> {
                    String testId = "google_" + i;
                    String testEmail = "test_" + i + "@gmail.com";
                    String testNickname = MENTOR_NICKNAME + i;
                    return createTestUser(testId, testEmail, testNickname);
                })
                .collect(Collectors.toList());  // collects the User objects into a List<User>

        // createdDate 값을 조절하기 위해, Thread.sleep() 사용: 하지만 MentorInfoResponse에서 시간 데이터를 사용하지 않으므로, 큰 의미는 없다.
        // 추후 리팩토링할때 무시할 것
        userRepository.saveAll(List.of(users.get(0), users.get(1)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(2), users.get(3), users.get(4)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(5), users.get(6), users.get(7)));

        // when
        // nickname은 중요하지 않다.
        Page<MentorInfoResponse> mentorList = mainPageService.getMentors("test1", pageRequest);

        // then
        // size 값은 SIZE 값과 동일하다.
        assertThat(mentorList.getSize()).isEqualTo(3);

        // content의 개수가 2개이다.
        assertThat(mentorList.getContent().size()).isEqualTo(2);

        MentorInfoResponse firstMentor = mentorList.getContent().get(0);
        MentorInfoResponse secondMentor = mentorList.getContent().get(1);

        assertThat(firstMentor.getNickname()).isEqualTo(MENTOR_NICKNAME + 7);
        assertThat(firstMentor.getImgUrl()).isNotBlank();
        assertThat(secondMentor.getNickname()).isEqualTo(MENTOR_NICKNAME + 8);
    }

    @Test
    @DisplayName("page=3, sort=3, order by createdDate, nickname DESC, mentor=8 : Content 0개 리턴")
    void getMentors_page_3() throws InterruptedException {
        // given
        int pageNum = 3;
        PageRequest pageRequest = PageRequest.of(pageNum, SIZE, SORT);

        // save 8 mentor data in users table
        // 헷갈림 방지를 위해, @BeforeEach에서 저장한 데이터 제거
        userRepository.deleteAll();

        int MENTOR_NUM = 8;
        String MENTOR_NICKNAME = "test_mentor_";
        List<User> users = IntStream.rangeClosed(1, MENTOR_NUM)
                .mapToObj(i -> {
                    String testId = "google_" + i;
                    String testEmail = "test_" + i + "@gmail.com";
                    String testNickname = MENTOR_NICKNAME + i;
                    return createTestUser(testId, testEmail, testNickname);
                })
                .collect(Collectors.toList());  // collects the User objects into a List<User>

        // createdDate 값을 조절하기 위해, Thread.sleep() 사용: 하지만 MentorInfoResponse에서 시간 데이터를 사용하지 않으므로, 큰 의미는 없다.
        // 추후 리팩토링할때 무시할 것
        userRepository.saveAll(List.of(users.get(0), users.get(1)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(2), users.get(3), users.get(4)));
        Thread.sleep(1000);

        userRepository.saveAll(List.of(users.get(5), users.get(6), users.get(7)));

        // when
        // nickname은 중요하지 않다.
        Page<MentorInfoResponse> mentorList = mainPageService.getMentors("test1", pageRequest);

        // then
        assertThat(mentorList.getContent().size()).isEqualTo(0);
    }

    /**
     * getFollowersOfUser
     */
    @Test
    @DisplayName("사용자의 팔로워가 3명인 경우")
    void getFollowersOfUser() {
        // given
        int MENTOR_NUM = 3;
        String MENTOR_NICKNAME = "test_mentor_";
        LocalDateTime now = LocalDateTime.now();

        List<Follow> follows = IntStream.rangeClosed(1, MENTOR_NUM)
                .mapToObj(i -> {
                    return Follow.of(TEST_MENTEE_NICKNAME, MENTOR_NICKNAME + i, now.plusMinutes(i));
                })
                .collect(Collectors.toList());

        List<User> users = IntStream.rangeClosed(1, MENTOR_NUM)
                .mapToObj(i -> {
                    String testId = "google_" + i;
                    String testEmail = "test_" + i + "@gmail.com";
                    String testNickname = MENTOR_NICKNAME + i;
                    return createTestUser(testId, testEmail, testNickname);
                })
                .collect(Collectors.toList());

        followRepository.saveAll(follows);
        userRepository.saveAll(users);

        // when
        // 여기서 User Table도 조회하기 때문에, User 엔티티도 db에 저장되어 있어야 한다.
        List<FollowUserResponse> followUserResponses = mainPageService.getFollowersOfUser(TEST_MENTEE_NICKNAME);

        // then
        assertThat(followUserResponses.size()).isEqualTo(MENTOR_NUM);

        FollowUserResponse followUserResponse1 = followUserResponses.get(0);
        FollowUserResponse followUserResponse2 = followUserResponses.get(1);
        FollowUserResponse followUserResponse3 = followUserResponses.get(2);

        // 팔로우를 가장 최근에 한, 즉 createdDate 값이 가장 큰 사용자가 인덱스 0인지 검증
        assertThat(followUserResponse1.getNickname()).isEqualTo(MENTOR_NICKNAME + "3");
        assertThat(followUserResponse2.getNickname()).isEqualTo(MENTOR_NICKNAME + "2");
        assertThat(followUserResponse3.getNickname()).isEqualTo(MENTOR_NICKNAME + "1");
    }

    /**
     * getLastAnsweredMessages
     */
    @Test
    @DisplayName("case 1: 멘토의 질문 답변 데이터가 0개인 경우(존재하지 않는 경우)")
    void getLastAnsweredMessages_returns_empty_ArrayList() {
        // given
        String id = "google_1234123124";
        String email = "mentor2@mentor.com";
        String nickname = "mentor_test_33";
        User mentor1 = createTestUser(id, email, nickname);

        // when
        List<String> lastAnsweredMessages = mainPageService.getLastAnsweredMessages(mentor1.getNickname());

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
                .mapToObj(i -> createTestQaList(mentor1, i))
                .collect(Collectors.toList());

        qaListRepository.saveAll(qaLists);

        // when
        List<String> lastAnsweredMessages = mainPageService.getLastAnsweredMessages(mentor1.getNickname());

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
                .mapToObj(i -> createTestQaList(mentor1, i))
                .collect(Collectors.toList());

        qaListRepository.saveAll(qaLists);

        // when
        List<String> lastAnsweredMessages = mainPageService.getLastAnsweredMessages(mentor1.getNickname());

        // then
        assertThat(lastAnsweredMessages.size()).isEqualTo(2);
    }

    private User createTestUser(String id, String email, String nickname) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .imgUrl("default/profile.png")  // set img url
                .build();
    }

    private QaList createTestQaList(User mentor, int index) {
        LocalDateTime now = LocalDateTime.now();
        return QaList.builder()
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .mentorNickname(mentor.getNickname())
                .questionOrigin("origin message_" + index)
                .questionSummary("summary message_" + index)
                .questionSummaryEn("summary eng message_" + index)
                .questionTime(now.minusHours(index))
                .answer("answer message_" + index)
                .answerTime(now)
                .build();
    }

}