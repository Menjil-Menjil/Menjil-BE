package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    private final String TEST_ROOM_ID = "test_room_1";
    private final String TEST_MENTEE_NICKNAME = "test_mentee_1";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_1";

    @BeforeEach
    void init() {
        Room room = Room.builder()
                .roomId(TEST_ROOM_ID).menteeNickname(TEST_MENTEE_NICKNAME)
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .build();
        roomRepository.save(room);
    }

    /**
     * createRoom()
     */
    @Test
    @DisplayName("방 생성이 정상적으로 완료되면, HttpStatus 201 값이 리턴된다.")
    void createRoom() {
        RoomDto roomDto = new RoomDto("test_room_2", "test_mentee_2", "test_mentor_2");

        int success = roomService.createRoom(roomDto);
        assertThat(success).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("방 생성시 방 ID 가 중복되면, HttpStatus 500 값이 리턴된다.")
    void roomIdAlreadyExists() {
        RoomDto roomDto = new RoomDto(TEST_ROOM_ID, "test_mentee_2", "test_mentor_2");

        int success = roomService.createRoom(roomDto);

        assertThat(success).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }


}