package seoultech.capstone.menjil.domain.chat.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.domain.Room;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("save로 Room Entity를 저장할 때 @CreatedDate 정상 작동 확인")
    @Rollback(value = false)
    void save() throws InterruptedException {
        // given
        Room room1 = Room.builder()
                .roomId("room1").menteeNickname("mentee1")
                .mentorNickname("멘토1")
                .build();
        Room room2 = Room.builder()
                .roomId("room2").menteeNickname("mentee2")
                .mentorNickname("멘토2")
                .build();

        roomRepository.save(room1);
        Thread.sleep(1000);
        roomRepository.save(room2);

        // when
        Room roomA = roomRepository.findRoomById("room1");
        Room roomB = roomRepository.findRoomById("room2");

        // then
        assertThat(roomB.getMenteeNickname()).isEqualTo("mentee2");
        assertThat(roomB.getMentorNickname()).isEqualTo("멘토2");

        // remove nanoseconds in MySQL datetime(6) column
        LocalDateTime roomATime = roomA.getCreatedDate();
        LocalDateTime roomBTime = roomB.getCreatedDate();
        assertThat(roomATime.isBefore(roomBTime)).isTrue();
    }

}