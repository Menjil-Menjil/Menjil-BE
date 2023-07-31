package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final static String TEST_ROOM_ID = "test_room_1";
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

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
    }

    /**
     * createRoom()
     */
    @Test
    @DisplayName("방 생성이 정상적으로 완료되면, HttpStatus 201 값이 리턴된다.")
    void createRoomSuccess() {
        RoomDto roomDto = new RoomDto("test_room_2", "test_mentee_2", "test_mentor_2");

        int success = roomService.createRoom(roomDto);
        assertThat(success).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("case 1: 방 생성시 멘토-멘티 간에 이미 생성된 대화방이 있다면, HttpStatus 500 값이 리턴된다.")
    void createRoomFail1() {
        RoomDto roomDto = new RoomDto("test_room_2", TEST_MENTEE_NICKNAME, TEST_MENTOR_NICKNAME);

        int success = roomService.createRoom(roomDto);
        assertThat(success).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("case 2: 방 생성시 room_id가 중복되면, HttpStatus 500 값이 리턴된다.")
    void createRoomFail2() {
        RoomDto roomDto = new RoomDto(TEST_ROOM_ID, "test_mentee_2", "test_mentor_2");

        int success = roomService.createRoom(roomDto);
        assertThat(success).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * enterTheRoom()
     */
    @Test
    @DisplayName("방 입장시 room_id가 db에 존재하지 않는 경우 CustomException 리턴")
    void enterTheRoomFail1() {
        String roomIdNotInDb = "test_room_2";

        assertThrows(CustomException.class, () -> roomService.enterTheRoom(roomIdNotInDb));
    }

    @Test
    @DisplayName("방 입장시 채팅 내역이 존재하지 않는 경우, Welcome Message를 보내준다")
    void enterTheRoomInNoChatMessage() {
        List<MessagesResponse> messageList = roomService.enterTheRoom(TEST_ROOM_ID);
        assertThat(messageList.size()).isEqualTo(1);

        MessagesResponse response = messageList.get(0);

        assertThat(response.getRoomId()).isEqualTo(TEST_ROOM_ID);
        assertThat(response.getSenderType()).isEqualTo(SenderType.MENTOR);
        assertThat(response.getMessageType()).isEqualTo(MessageType.ENTER);
    }

    @Test
    @DisplayName("방 입장시 채팅 내역이 이미 존재하는 경우, 기존 메시지들을 응답으로 보낸다")
    void enterTheRoomInChatMessage() {
        LocalDateTime now = LocalDateTime.now();

        List<ChatMessage> saveThreeMessages = Arrays.asList(
                ChatMessage.builder()
                        ._id("test_uuid_1")
                        .roomId(TEST_ROOM_ID)
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("test message 1")
                        .messageType(MessageType.TALK)
                        .time(now)
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_2")
                        .roomId(TEST_ROOM_ID)
                        .senderType(SenderType.MENTEE)
                        .senderNickname("test_mentee_nickname")
                        .message("test message 2")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000))
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_3")
                        .roomId(TEST_ROOM_ID)
                        .senderType(SenderType.MENTOR)
                        .senderNickname("test_mentor_nickname")
                        .message("mentor's response")
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(5000))
                        .build()
        );
        messageRepository.saveAll(saveThreeMessages);

        List<MessagesResponse> messageList = roomService.enterTheRoom(TEST_ROOM_ID);
        assertThat(messageList.size()).isEqualTo(3);

        // 대화는 챗봇 형식, 즉 일대일로 진행되므로, 멘티와 멘토 타입이 존재할 수밖에 없다.
        boolean menteeExists = messageList.stream().anyMatch(
                t -> SenderType.MENTEE.equals(messageList.get(1).getSenderType())
                        || SenderType.MENTEE.equals(messageList.get(0).getSenderType()
                ));
        boolean mentorExists = messageList.stream().anyMatch(
                t -> SenderType.MENTOR.equals(messageList.get(1).getSenderType())
                        || SenderType.MENTOR.equals(messageList.get(0).getSenderType()
                ));
        assertThat(menteeExists).isTrue();
        assertThat(mentorExists).isTrue();
    }

}