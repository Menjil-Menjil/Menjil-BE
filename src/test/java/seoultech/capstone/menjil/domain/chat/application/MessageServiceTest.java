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
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageRepository messageRepository;

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

        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .menteeNickname(menteeNickname)
                .mentorNickname(mentorNickname)
                .build();

        // when
        MessagesResponse result = messageService.sendWelcomeMessage(roomDto, roomId);

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
    @DisplayName("클라이언트로 들어오는 채팅 메시지가 db에 저장이 정상적으로 되는 경우 true 리턴")
    void saveChatMessage() {
        // given
        LocalDateTime now = LocalDateTime.now();
        MessageDto messageDto = MessageDto.builder()
                .roomId("test_room_3")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now.toString())
                .build();

        // when
        boolean result = messageService.saveChatMessage(messageDto);

        // then
        assertThat(result).isTrue();
    }

}