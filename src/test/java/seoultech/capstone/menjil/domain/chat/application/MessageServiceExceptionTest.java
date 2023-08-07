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
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceExceptionTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    /**
     * sendWelcomeMessage()
     */
    @Test
    void sendWelcomeMessage_Should_Throw_CustomException_WhenSaveFails() {
        // Arrange
        String roomId = "test_room_id";
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor")
                .menteeNickname("test_mentee")
                .build();

        // DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(messageRepository).save(any(ChatMessage.class));

        // Act and Assert
        assertThrows(CustomException.class, () -> messageService.sendWelcomeMessage(roomDto, roomId));
    }

    /**
     * saveChatMessage()
     */
    @Test
    void saveChatMessage_Should_Return_False_WhenSaveFails() {
        // Arrange
        MessageDto messageDto = MessageDto.builder()
                .roomId("test1")
                .message("hello Message")
                .messageType(MessageType.ENTER)
                .senderType(SenderType.MENTOR)
                .time(LocalDateTime.now().toString())
                .build();
        when(messageRepository.save(any(ChatMessage.class))).thenThrow(new DataIntegrityViolationException("Error"));

        // Act
        boolean result = messageService.saveChatMessage(messageDto);

        // Assert
        assertFalse(result);
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

}
