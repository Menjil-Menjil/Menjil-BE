package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-database-test.yml" +
        ",classpath:/application-jwt.properties")
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("방 생성이 정상적으로 완료되면, status 201 값이 리턴된다.")
    void createRoom() {
        RoomDto roomDto = new RoomDto("testroom1", "testmentee1", "testmentor1");

        int success = roomService.createRoom(roomDto);
        assertThat(success).isEqualTo(HttpStatus.CREATED.value());
    }
}