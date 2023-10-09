package seoultech.capstone.menjil.domain.chat.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static seoultech.capstone.menjil.global.exception.ErrorIntValue.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
public class MessageServiceExceptionTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    private final static String TEST_ROOM_ID = "test_room_1";
    private final String TEST_MENTEE_NICKNAME = "test_mentee_1";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_1";

    /**
     * sendWelcomeMessage
     */
    @Test
    @DisplayName("db에 저장 실패한 경우")
    void sendWelcomeMessage_return_Optional_empty_WhenSaveFails() {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();

        // when: DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(messageRepository).save(any(ChatMessage.class));

        // then
        Assertions.assertThat(messageService.sendWelcomeMessage(roomDto)).isEmpty();
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

    /**
     * saveChatMessage
     */
    @Test
    @DisplayName("db에 저장 실패한 경우 int INTERNAL_SERVER_ERROR 리턴")
    void saveChatMessage_Return_INTERNAL_SERVER_ERROR_WhenSaveFails() {
        // given
        String formattedDateTime = createTimeFormatOfMessageResponse(LocalDateTime.now());

        MessageRequest messageDto = MessageRequest.builder()
                .roomId(TEST_ROOM_ID)
                .senderNickname(TEST_MENTOR_NICKNAME)
                .message("hello Message")
                .messageType(MessageType.ENTER)
                .senderType(SenderType.MENTOR)
                .time(formattedDateTime)
                .build();

        // when
        when(messageRepository.save(any(ChatMessage.class))).thenThrow(new DataIntegrityViolationException("Error"));

        // then
        int result = messageService.saveChatMessage(messageDto);
        assertEquals(result, INTERNAL_SERVER_ERROR.getValue());
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

    /**
     * sendAIMessage
     */
    // TODO: 테스트 코드 작성 실패. 추후 다시 시도할 예정
//    @Test
//    @DisplayName("db에 저장 실패한 경우 null 리턴")
//    void sendAIMessage_Return_null_WhenSaveFails() {
//        // given
//        String formattedDateTime = createTimeFormatOfMessageResponse(LocalDateTime.now());
//
//        MessageRequest clientMessageDto = MessageRequest.builder()
//                .roomId(TEST_ROOM_ID)
//                .senderType(SenderType.MENTEE)
//                .senderNickname(TEST_MENTEE_NICKNAME)
//                .message("멘티의 질문입니다")
//                .messageType(MessageType.C_QUESTION)
//                .time(formattedDateTime)
//                .build();
//
//        // when
//        when(messageRepository.save(any(ChatMessage.class))).thenThrow(new DataIntegrityViolationException("Error"));
//
//        // then
//        Assertions.assertThat(messageService.sendAIMessage(TEST_ROOM_ID, clientMessageDto)).isNull();
//        verify(messageService, times(1)).sendAIMessage(TEST_ROOM_ID, clientMessageDto);
//        verify(messageRepository, times(1)).save(any(ChatMessage.class));
//    }
    private String createTimeFormatOfMessageResponse(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }
}
