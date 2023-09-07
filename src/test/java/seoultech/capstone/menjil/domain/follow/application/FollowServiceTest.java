package seoultech.capstone.menjil.domain.follow.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.follow.dto.request.FollowRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class FollowServiceTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    private final String TEST_USER_NICKNAME = "test_user_nickname";
    private final String TEST_FOLLOW_NICKNAME = "test_follow_nickname";
    private final int FOLLOW_CREATED = 0;
    private final int FOLLOW_DELETED = 1;

    @BeforeEach
    void setUp() {
        LocalDateTime time = LocalDateTime.now().withNano(0);   // ignore milliseconds
        Follow testFollow = Follow.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME, time);
        followRepository.save(testFollow);
    }

    /**
     * followRequest
     */
    @Test
    @DisplayName("db에 이미 팔로우 내용이 존재하는 경우, FOLLOW_DELETED=1 을 리턴한다")
    void followRequest_return_FOLLOW_DELETED() {
        // given
        FollowRequest followRequest = FollowRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);

        // when
        // 실행하기 전 db에 데이터가 존재하는지 검증
        assertThat(followRepository.findAll().size()).isOne();

        int result = followService.followRequest(followRequest);

        // then
        assertThat(result).isEqualTo(FOLLOW_DELETED);

        // db에서 지워졌는지 검증
        assertThat(followRepository.findAll().size()).isZero();
    }

    @Test
    @DisplayName("팔로우 내용이 db에 존재하지 않는 경우, FOLLOW_CRETAED=0 을 리턴한다")
    void followRequest_return_FOLLOW_CRETAED() {
        // given
        String testUser = "testUser";
        FollowRequest followRequest = FollowRequest.of(testUser, TEST_FOLLOW_NICKNAME);

        // when
        // 실행하기 전 db에 데이터가 존재하는지 검증
        assertThat(followRepository.findAll().size()).isOne();

        int result = followService.followRequest(followRequest);

        // then
        assertThat(result).isEqualTo(FOLLOW_CREATED);

        // db에 추가되었는지 검증
        assertThat(followRepository.findAll().size()).isEqualTo(2);
    }

    /**
     * checkFollowStatus
     */
    @Test
    @DisplayName("팔로우가 db에 이미 존재하는 경우 True 리턴")
    void checkFollowStatus_return_true() {
        // when
        boolean result = followService.checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("팔로우가 db에 존재하지 않는 경우 False 리턴")
    void checkFollowStatus_return_false() {
        // given
        String testUser = "testLala";
        String testFollow = "testFollow";

        // when
        // user가 존재하지 않는 경우
        boolean result1 = followService.checkFollowStatus(testUser, TEST_FOLLOW_NICKNAME);

        // follow가 존재하지 않는 경우
        boolean result2 = followService.checkFollowStatus(TEST_USER_NICKNAME, testFollow);

        // then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }

    /**
     * followIsExist
     */
    @Test
    @DisplayName("db에 사용자의 팔로우 내용이 존재하는지 검증. 존재한다면 true 리턴")
    void followIsExist_return_true_when_exists_in_DB() {
        // given
        Optional<Follow> follow = followRepository
                .findFollowByUserNicknameAndFollowNickname(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);

        // when
        boolean result = followService.followIsExist(follow);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("db에 사용자의 팔로우 내용이 존재하는지 검증. 존재하지 않는다면 false 리턴")
    void followIsExist_return_false_when_not_exists_in_DB() {
        // given
        // user가 존재하지 않는 경우
        String testUser = "testLala";
        Optional<Follow> follow1 = followRepository
                .findFollowByUserNicknameAndFollowNickname(testUser, TEST_FOLLOW_NICKNAME);

        // follow가 존재하지 않는 경우
        String testFollow = "testFollow";
        Optional<Follow> follow2 = followRepository
                .findFollowByUserNicknameAndFollowNickname(TEST_USER_NICKNAME, testFollow);

        // when
        boolean result1 = followService.followIsExist(follow1);
        boolean result2 = followService.followIsExist(follow2);

        // then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }
}