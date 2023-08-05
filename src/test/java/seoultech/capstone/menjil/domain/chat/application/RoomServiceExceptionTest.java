package seoultech.capstone.menjil.domain.chat.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class RoomServiceExceptionTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    /**
     * enterTheRoom()
     */
    @Test
    void enterTheRoom_Should_ThrowCustomException_WhenSaveFails() {
        // Arrange
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname("test_mentor")
                .menteeNickname("test_mentee")
                .build();
        String roomId = "test_room_id";

        // DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(roomRepository).save(any(Room.class));

        // Act and Assert
        assertThrows(CustomException.class, () -> roomService.enterTheRoom(roomDto, roomId));
    }
}
