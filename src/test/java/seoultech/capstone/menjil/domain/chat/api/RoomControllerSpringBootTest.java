package seoultech.capstone.menjil.domain.chat.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class RoomControllerSpringBootTest {

    @Autowired
    private RoomController roomController;

    /**
     * chatMessageIsMoreThanOne
     */
    @Test
    @DisplayName("case 1: List의 개수가 2개이면 true 리턴")
    void chatMessageIsMoreThanOne_return_true() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<MessageResponse> messageResponses = Arrays.asList(
                MessageResponse.builder()
                        ._id("test_uuid_1")
                        .order(1)
                        .roomId("test_room_1")
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now)
                        .build(),
                MessageResponse.builder()
                        ._id("test_uuid_2")
                        .roomId("test_room_2")
                        .order(2)
                        .senderType(SenderType.MENTEE)
                        .senderNickname("test_mentee_nickname")
                        .message("test message 2")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000))
                        .build()
        );

        // when
        boolean result = roomController.chatMessageIsMoreThanOne(messageResponses);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("case 2: List의 개수가 1 이하면 false 리턴, order = null")
    void chatMessageIsMoreThanOne_return_false_and_order_is_null() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<MessageResponse> response = Collections.singletonList(MessageResponse.builder()
                ._id("test_uuid_1")
                .order(null)   // here is null
                .roomId("test_room_id")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        // when
        boolean result = roomController.chatMessageIsMoreThanOne(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("case 2-1: List의 개수가 1 이하면 false 리턴, order = 1")
    void chatMessageIsMoreThanOne_return_false_and_order_is_not_null() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<MessageResponse> response = Collections.singletonList(MessageResponse.builder()
                ._id("test_uuid_1")
                .order(1)   // here is not null
                .roomId("test_room_id")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        // when
        boolean result = roomController.chatMessageIsMoreThanOne(response);

        // then
        assertThat(result).isFalse();
    }

    /**
     * checkIfUserEnterTheRoomAtFirstTime
     */
    @Test
    @DisplayName("case 1: order가 null이면 true 리턴")
    void checkIfUserEnterTheRoomAtFirstTime_return_true_when_order_is_null() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<MessageResponse> response = Collections.singletonList(MessageResponse.builder()
                ._id("test_uuid_1")
                .order(null)   // here is null
                .roomId("test_room_id")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        // when
        boolean result = roomController.checkIfUserEnterTheRoomAtFirstTime(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("case 2: order가 1이면 false 리턴")
    void checkIfUserEnterTheRoomAtFirstTime_return_true_when_order_is_not_null() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<MessageResponse> response = Collections.singletonList(MessageResponse.builder()
                ._id("test_uuid_1")
                .order(1)   // here is not null
                .roomId("test_room_id")
                .senderType(SenderType.MENTOR)
                .senderNickname("test_mentor_nickname")
                .message("Welcome Message")
                .messageType(MessageType.ENTER)
                .time(now)
                .build());

        // when
        boolean result = roomController.checkIfUserEnterTheRoomAtFirstTime(response);

        // then
        assertThat(result).isFalse();
    }

    /**
     * userHasNoRooms
     */
    @Test
    @DisplayName("case 1: 빈 리스트 객체일 경우, true를 리턴한다")
    void userHasNoRooms_when_return_true() {
        // given
        List<RoomInfoResponse> list = new ArrayList<>();

        // when
        boolean result = roomController.userHasNoRooms(list);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("case 2: 빈 리스트 객체가 아니면, false를 리턴한다")
    void userHasNoRooms_when_return_false() {
        // given
        String nickname = "test";
        String imgUrl = "imgurl";
        String msg = "hello";
        LocalDateTime time = LocalDateTime.now();

        List<RoomInfoResponse> list = List.of(RoomInfoResponse.of("room1", nickname,
                        imgUrl, msg, time),
                RoomInfoResponse.of("room2", nickname + "1",
                        imgUrl + "1", msg + "1", time.plusSeconds(1000)));

        // when
        boolean result = roomController.userHasNoRooms(list);

        // then
        assertThat(result).isFalse();
    }
}
