package seoultech.capstone.menjil.domain.chat.application;

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
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceExceptionTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    private final int TIME_INPUT_INVALID = 0;
    private final int INTERNAL_SERVER_ERROR = 1;
    private final int SAVE_SUCCESS = 100;

    /**
     * sendWelcomeMessage()
     */
    @Test
    void sendWelcomeMessage_Should_Throw_CustomException_WhenSaveFails() {
        // Arrange
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname("test_mentor")
                .menteeNickname("test_mentee")
                .roomId("test_room_id")
                .build();

        // DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(messageRepository).save(any(ChatMessage.class));

        // Act and Assert
        assertThrows(CustomException.class, () -> messageService.sendWelcomeMessage(roomDto));
    }

    /**
     * saveChatMessage()
     */
    @Test
    void saveChatMessage_Should_Return_False_WhenSaveFails() {
        // Arrange
        LocalDateTime now = LocalDateTime.now().withNano(0);    // ignore milliseconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);

        MessageRequest messageDto = MessageRequest.builder()
                .roomId("test1")
                .message("hello Message")
                .messageType(MessageType.ENTER)
                .senderType(SenderType.MENTOR)
                .time(formattedDate)
                .build();
        when(messageRepository.save(any(ChatMessage.class))).thenThrow(new DataIntegrityViolationException("Error"));

        // Act
        int result = messageService.saveChatMessage(messageDto);

        // Assert
        assertEquals(result, INTERNAL_SERVER_ERROR);
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

}
