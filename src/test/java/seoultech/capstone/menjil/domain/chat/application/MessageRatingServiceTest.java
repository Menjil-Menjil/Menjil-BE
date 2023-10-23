package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageClickIncViewsAndLikesRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageClickIncViewsAndLikesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MessageRatingServiceTest {

    @Autowired
    private MessageRatingService messageRatingService;

    @Autowired
    private QaListRepository qaListRepository;

    private final Long DEFAULT_VALUE = 0L;
    private String DOCUMENT_ID = "";

    // TODO: 추후 테스트 코드 수정 예정
    /*
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        QaList qaList1 = QaList.builder()
                .menteeNickname("mentee")
                .mentorNickname("mentor_")
                .questionOrigin("origin message_")
                .questionSummary("summary message_")
                .questionSummaryEn("summary eng message_")
                .questionTime(now.minusHours(1000))
                .answer("answer message_")
                .answerTime(LocalDateTime.now())
                .build();

        // set views and likes
        qaList1.setViews(DEFAULT_VALUE);
        qaList1.setLikes(DEFAULT_VALUE);
        qaListRepository.save(qaList1);

        // MessageClickIncViewsAndLikesRequest에서 _id 값이 필요하므로 미리 가져옴
        DOCUMENT_ID = qaList1.get_id();
    }

    @AfterEach
    void tearDown() {
        // delete mongodb manually
        qaListRepository.deleteAll();
    }

    @Test
    @DisplayName("case 1: QaList id가 db에 존재하지 않는 경우, 예외처리")
    void incrementViewsAndLikes_id_not_inDB() {
        // given
        String _id = "not2fadsf13vzv";  // random value
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(_id, true);

        // then
        assertThrows(CustomException.class, () -> messageRatingService.incrementViewsAndLikes(request));
    }

    @Test
    @DisplayName("case 2: 정상 로직, 좋아요를 누른 경우")
    void incrementViewsAndLikes_add_likes() {
        // given
        String _id = DOCUMENT_ID;  // saved value
        Boolean likes = true;
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(_id, likes);

        // when
        MessageClickIncViewsAndLikesResponse response = messageRatingService.incrementViewsAndLikes(request);

        // then
        int updateNum = 1;
        assertThat(response.getQuestionId()).isEqualTo(_id);
        assertThat(response.getViews()).isEqualTo(DEFAULT_VALUE + updateNum);
        assertThat(response.getLikes()).isEqualTo(DEFAULT_VALUE + updateNum);
    }

    @Test
    @DisplayName("case 2-1: 정상 로직, 좋아요를 누르지 않은 경우")
    void incrementViewsAndLikes_no_likes() {
        // given
        String _id = DOCUMENT_ID;  // saved value
        Boolean likes = false;
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(_id, likes);

        // when
        MessageClickIncViewsAndLikesResponse response = messageRatingService.incrementViewsAndLikes(request);

        // then
        int updateNum = 1;
        assertThat(response.getQuestionId()).isEqualTo(_id);
        assertThat(response.getViews()).isEqualTo(DEFAULT_VALUE + updateNum);
        assertThat(response.getLikes()).isEqualTo(DEFAULT_VALUE);
    }

    */
}