package seoultech.capstone.menjil.domain.chatbot.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chatbot.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chatbot.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;
import seoultech.capstone.menjil.domain.chatbot.domain.QaList;
import seoultech.capstone.menjil.domain.chatbot.domain.SenderType;
import seoultech.capstone.menjil.domain.chatbot.dto.request.MessageClickIncViewsAndLikesRequest;
import seoultech.capstone.menjil.domain.chatbot.dto.response.MessageClickIncViewsAndLikesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MessageRatingServiceTest {

    @Autowired
    private MessageRatingService messageRatingService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private QaListRepository qaListRepository;

    private final Long DEFAULT_VALUE = 0L;
    private String CHAT_MESSAGE_DOCUMENT_ID = "";
    private String QA_LIST_DOCUMENT_ID = "";
    private final MessageType RATING_TYPE = MessageType.AI_SUMMARY_RATING;

    // TODO: 추후 테스트 코드 수정 예정

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage chatMessage1 = ChatMessage.builder()
                .roomId("test_room_id")
                .senderType(SenderType.AI)
                .senderNickname("mentor_")
                .message("삭제되어야 할 메시지")
                .messageList(null)
                .messageType(MessageType.AI_SUMMARY_RATING)
                .time(now.minusHours(1000))
                .build();
        messageRepository.save(chatMessage1);

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

        // _id 값이 필요하므로 미리 가져옴
        CHAT_MESSAGE_DOCUMENT_ID = chatMessage1.get_id();
        QA_LIST_DOCUMENT_ID = qaList1.get_id();
    }

    @AfterEach
    void tearDown() {
        // delete mongodb manually
        messageRepository.deleteAll();
        qaListRepository.deleteAll();
    }

    @Test
    @DisplayName("case 1: ChatMessage id가 db에 존재하지 않는 경우, 예외처리")
    void incrementViewsAndLikes_chat_message_not_inDB() {
        // given
        String Id = "test1";  // 1st check.
        String questionId = "not3344by2277";  // random value, 2nd check
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(Id, questionId, true);

        // then
        assertThrows(CustomException.class, () -> messageRatingService.incrementViewsAndLikes(request));
    }


    @Test
    @DisplayName("case 2: QaList id가 db에 존재하지 않는 경우, 예외처리")
    void incrementViewsAndLikes_qaList_id_not_inDB() {
        // given
        String Id = CHAT_MESSAGE_DOCUMENT_ID;
        String questionId = "not3344by2277";  // random value
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(Id, questionId, true);

        // then
        assertThrows(CustomException.class, () -> messageRatingService.incrementViewsAndLikes(request));
    }

    @Test
    @DisplayName("case 3: 정상 로직, 좋아요를 누른 경우")
    void incrementViewsAndLikes_add_likes() {
        // given
        String Id = CHAT_MESSAGE_DOCUMENT_ID;
        String questionId = QA_LIST_DOCUMENT_ID;
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(Id, questionId, true);

        // 먼저 db에 데이터가 존재하는지 검증
        Optional<ChatMessage> savedMessage = messageRepository.findBy_idAndMessageType(Id, RATING_TYPE);
        assertThat(savedMessage.isPresent()).isTrue();

        // when
        MessageClickIncViewsAndLikesResponse response = messageRatingService.incrementViewsAndLikes(request);

        // then
        int updateNum = 1;
        assertThat(response.getQuestionId()).isEqualTo(questionId);
        assertThat(response.getViews()).isEqualTo(DEFAULT_VALUE + updateNum);
        assertThat(response.getLikes()).isEqualTo(DEFAULT_VALUE + updateNum);

        // 메서드 실행 이후 db에 데이터가 지워졌는지 검증
        Optional<ChatMessage> findMessage = messageRepository.findBy_idAndMessageType(Id, RATING_TYPE);
        assertThat(findMessage.isEmpty()).isTrue();
        assertThat(messageRepository.findAll().size()).isZero();
    }

    @Test
    @DisplayName("case 3-1: 정상 로직, 좋아요를 누르지 않은 경우")
    void incrementViewsAndLikes_no_likes() {
        // given
        String Id = CHAT_MESSAGE_DOCUMENT_ID;
        String questionId = QA_LIST_DOCUMENT_ID;

        // here is false
        MessageClickIncViewsAndLikesRequest request = new MessageClickIncViewsAndLikesRequest(Id, questionId, false);

        // 먼저 db에 데이터가 존재하는지 검증
        Optional<ChatMessage> savedMessage = messageRepository.findBy_idAndMessageType(Id, RATING_TYPE);
        assertThat(savedMessage.isPresent()).isTrue();

        // when
        MessageClickIncViewsAndLikesResponse response = messageRatingService.incrementViewsAndLikes(request);

        // then
        int updateNum = 1;
        assertThat(response.getQuestionId()).isEqualTo(questionId);
        assertThat(response.getViews()).isEqualTo(DEFAULT_VALUE + updateNum);
        assertThat(response.getLikes()).isEqualTo(DEFAULT_VALUE);

        // 메서드 실행 이후 db에 데이터가 지워졌는지 검증
        Optional<ChatMessage> findMessage = messageRepository.findBy_idAndMessageType(Id, RATING_TYPE);
        assertThat(findMessage.isEmpty()).isTrue();
        assertThat(messageRepository.findAll().size()).isZero();
    }


}