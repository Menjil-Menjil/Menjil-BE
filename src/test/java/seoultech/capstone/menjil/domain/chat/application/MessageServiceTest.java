package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageRepository messageRepository;

    private final int TIME_INPUT_INVALID = 0;
    private final int SAVE_SUCCESS = 100;

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
    }

    /**
     * sendWelcomeMessage()
     */
    @Test
    @DisplayName("정상적으로 응답 메시지가 생성되며, MessagesResponse Dto 객체가 리턴된다")
    void sendWelcomeMessage() {
        // given
        String roomId = "room_id_one";
        String menteeNickname = "test_mentee_one";
        String mentorNickname = "test_mentor_one";

        RoomDto roomDto = RoomDto.builder()
                .menteeNickname(menteeNickname)
                .mentorNickname(mentorNickname)
                .roomId(roomId)
                .build();

        // when
        MessageResponse result = messageService.sendWelcomeMessage(roomDto);

        // then
        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getOrder()).isNull();
        assertThat(result.getSenderType()).isEqualTo(SenderType.MENTOR);
        assertThat(result.getSenderNickname()).isEqualTo(mentorNickname);
        assertThat(result.getMessage()).isNotBlank();   // check that a string is not null, not empty, and not just whitespace.
        assertThat(result.getMessageType()).isEqualTo(MessageType.ENTER);
    }

    /**
     * saveChatMessage()
     */
    @Test
    @DisplayName("클라이언트로 들어오는 채팅 메시지가 db에 저장이 정상적으로 되는 경우 int 100 리턴")
    void saveChatMessage() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);    // ignore milliseconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);

        MessageRequest messageDto = MessageRequest.builder()
                .roomId("test_room_3")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(formattedDate)
                .build();

        // when
        int result = messageService.saveChatMessage(messageDto);

        // then
        assertThat(result).isEqualTo(SAVE_SUCCESS);
    }

    @Test
    @DisplayName("MessageDto에서 time 형식이 올바르지 않은 경우 int 0 리턴")
    void saveChatMessage_time_format_is_not_alright() {
        // given
        String format_is_wrong = "2023:08:14T12:23:00";
        String format_is_wrong_v2 = "2023:08:14  12:23:00"; // 공백 2칸

        MessageRequest messageDto1 = MessageRequest.builder()
                .roomId("test_room_3")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(format_is_wrong)
                .build();

        MessageRequest messageDto2 = MessageRequest.builder()
                .roomId("test_room_3")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(format_is_wrong_v2)
                .build();

        // when
        int result1 = messageService.saveChatMessage(messageDto1);
        int result2 = messageService.saveChatMessage(messageDto2);

        // then
        assertThat(result1).isEqualTo(TIME_INPUT_INVALID);
        assertThat(result2).isEqualTo(TIME_INPUT_INVALID);
    }
}