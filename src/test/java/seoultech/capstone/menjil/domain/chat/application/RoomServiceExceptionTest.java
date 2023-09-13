package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;

import static com.mongodb.assertions.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceExceptionTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MessageRepository messageRepository;

    private final String TEST_ROOM_ID = "test_room_1";
    private final String TEST_MENTEE_NICKNAME = "test_mentee_1";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_1";


    /**
     * deleteChatMessagesByRoomId
     */
    @Test
    @DisplayName("db에서 메세지 삭제를 실패한 경우")
    void deleteChatMessagesByRoomId_return_false_whenSaveFails() {
        // given
        String roomId = TEST_ROOM_ID;

        // when: DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(messageRepository)
                .deleteChatMessagesByRoomId(roomId);

        // An exception is thrown, so the method should return false
        assertFalse(roomService.deleteChatMessagesByRoomId(roomId));

        // Verify that the repository method was called once
        verify(messageRepository, times(1)).deleteChatMessagesByRoomId(roomId);
    }

    /**
     * deleteRoomByRoomId
     */
    @Test
    @DisplayName("db에서 방 삭제를 실패한 경우")
    void deleteRoomByRoomId_return_false_whenSaveFails() {
        // given
        String roomId = TEST_ROOM_ID;

        // when: DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(roomRepository)
                .deleteRoomById(roomId);

        // An exception is thrown, so the method should return false
        assertFalse(roomService.deleteRoomByRoomId(roomId));

        // Verify that the repository method was called once
        verify(roomRepository, times(1)).deleteRoomById(roomId);
    }
}
