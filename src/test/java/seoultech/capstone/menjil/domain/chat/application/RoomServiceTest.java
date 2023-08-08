package seoultech.capstone.menjil.domain.chat.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import seoultech.capstone.menjil.domain.chat.dto.response.RoomListResponse;
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
    private final String TYPE_MENTOR = "MENTOR";
    private final String TYPE_MENTEE = "MENTEE";

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
     * enterTheRoom()
     */
    @Test
    @DisplayName("방 입장시 채팅방이 db에 존재하지 않는 경우, Welcome Message를 보내준다")
    void enterTheRoom_Room_Not_Exists() {
        // given
        String menteeNickname = TEST_MENTEE_NICKNAME + "no";
        String mentorNickname = TEST_MENTOR_NICKNAME + "no";
        String roomId = TEST_ROOM_ID + "no";

        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname(mentorNickname)
                .menteeNickname(menteeNickname)
                .roomId(roomId)
                .build();

        List<MessagesResponse> messageList = roomService.enterTheRoom(roomDto);
        assertThat(messageList.size()).isEqualTo(1);

        MessagesResponse response = messageList.get(0);

        assertThat(response.getRoomId()).isEqualTo(roomId);
        assertThat(response.getSenderNickname()).isEqualTo(mentorNickname); // Welcome Message is sent by mentor
        assertThat(response.getSenderType()).isEqualTo(SenderType.MENTOR);
        assertThat(response.getMessageType()).isEqualTo(MessageType.ENTER);
    }

    @Test
    @DisplayName("방 입장시 채팅방이 db에 존재하는 경우, db에 저장된 메시지들을 응답으로 보낸다")
    void enterTheRoom_when_Room_already_exists() {
        RoomDto roomDto = RoomDto.roomDtoConstructor()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();
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

        List<MessagesResponse> messageList = roomService.enterTheRoom(roomDto);
        assertThat(messageList.size()).isEqualTo(3);

        // 대화는 챗봇 형식, 즉 일대일로 진행되므로, 멘티와 멘토 타입이 존재할 수밖에 없다.
        List<SenderType> senderTypesList = Arrays.asList(messageList.get(0).getSenderType(),
                messageList.get(1).getSenderType(), messageList.get(2).getSenderType());
        boolean menteeExists = senderTypesList.stream().anyMatch(
                type -> type.equals(SenderType.MENTEE));
        boolean mentorExists = senderTypesList.stream().anyMatch(
                type -> type.equals(SenderType.MENTOR));
        assertThat(menteeExists).isTrue();
        assertThat(mentorExists).isTrue();

        // Order 1, 2, 3이 정상적으로 리턴되는지 확인
        List<Integer> orderList = Arrays.asList(messageList.get(0).getOrder(), messageList.get(1).getOrder(),
                messageList.get(2).getOrder());
        boolean order1Exists = orderList.stream().anyMatch(
                num -> num == 1);
        boolean order2Exists = orderList.stream().anyMatch(
                num -> num == 2);
        boolean order3Exists = orderList.stream().anyMatch(
                num -> num == 3);
        boolean order4Exists = orderList.stream().anyMatch(
                num -> num == 4);
        assertThat(order1Exists).isTrue();
        assertThat(order2Exists).isTrue();
        assertThat(order3Exists).isTrue();
        assertThat(order4Exists).isFalse(); // order 4 not exists because of the number of data is 3
    }

    /**
     * getAllRooms()
     */
    @Test
    @DisplayName("멘티가 멘토링 페이지를 조회하면, RoomListResponse 객체 3개가 리턴된다")
    void getAllRooms_By_MENTEE() {
        // given
        String room2Id = TEST_ROOM_ID + "room2";
        String room2MentorNickname = TEST_MENTOR_NICKNAME + "room2";
        String room2Msg = "test message 2";

        String room3Id = TEST_ROOM_ID + "room3";
        String room3MentorNickname = TEST_MENTOR_NICKNAME + "room3";
        String room3Msg = "mentor's response";

        // save rooms
        Room room2 = Room.builder()
                .roomId(room2Id).menteeNickname(TEST_MENTEE_NICKNAME)
                .mentorNickname(room2MentorNickname)
                .build();
        Room room3 = Room.builder()
                .roomId(room3Id).menteeNickname(TEST_MENTEE_NICKNAME)
                .mentorNickname(room3MentorNickname)
                .build();
        roomRepository.saveAll(List.of(room2, room3));

        // save messages
        // 주의: roomId가 room, room2, room3의 아이디와 동일해야 한다.
        LocalDateTime now = LocalDateTime.now();
        List<ChatMessage> saveThreeMessages = Arrays.asList(
                ChatMessage.builder()
                        ._id("test_uuid_1")
                        .roomId(TEST_ROOM_ID)
                        .senderType(SenderType.MENTEE)
                        .senderNickname(TEST_MENTEE_NICKNAME)
                        .message("test message 1")
                        .messageType(MessageType.TALK)
                        .time(now)
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_2")
                        .roomId(room2Id)
                        .senderType(SenderType.MENTEE)
                        .senderNickname(room2MentorNickname)
                        .message(room2Msg)
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000))
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_3")
                        .roomId(room3Id)
                        .senderType(SenderType.MENTOR)
                        .senderNickname(room3MentorNickname)
                        .message(room3Msg)
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(5000))
                        .build()
        );
        messageRepository.saveAll(saveThreeMessages);

        // when
        List<RoomListResponse> getRoomList = roomService.getAllRooms(TEST_MENTEE_NICKNAME, TYPE_MENTEE);

        // then
        // test if size is 3
        assertThat(getRoomList.size()).isEqualTo(3);

        // test if room2Msg, room3Msg contains correctly
        List<String> messageList = Arrays.asList(getRoomList.get(0).getLastMessage(),
                getRoomList.get(1).getLastMessage(), getRoomList.get(2).getLastMessage());
        boolean room2MsgExists = messageList.stream().anyMatch(
                msg -> msg.equals(room2Msg));
        boolean room3MsgExists = messageList.stream().anyMatch(
                msg -> msg.equals(room2Msg));
        assertThat(room2MsgExists).isTrue();
        assertThat(room3MsgExists).isTrue();
    }

    @Test
    @DisplayName("멘티가 멘토링 페이지를 조회하였으나, 데이터가 없는 경우 size가 0이다")
    void getAllRooms_By_MENTEE_when_data_is_Null() {
        String notExistsMenteeNickname = "mentee_haha_hoho";
        List<RoomListResponse> getRoomList = roomService.getAllRooms(notExistsMenteeNickname, TYPE_MENTEE);

        assertThat(getRoomList.size()).isZero();
    }

    @Test
    @DisplayName("멘토가 멘토링 페이지를 조회하면, RoomListResponse 객체 3개가 리턴된다")
    void getAllRooms_By_MENTOR() {
        // given
        String room2Id = TEST_ROOM_ID + "room2";
        String room2MenteeNickname = TEST_MENTEE_NICKNAME + "room2";
        String room2Msg = "test message 2";

        String room3Id = TEST_ROOM_ID + "room3";
        String room3MenteeNickname = TEST_MENTEE_NICKNAME + "room3";
        String room3Msg = "mentor's response";

        // save rooms
        Room room2 = Room.builder()
                .roomId(room2Id).menteeNickname(room2MenteeNickname)
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .build();
        Room room3 = Room.builder()
                .roomId(room3Id).menteeNickname(room3MenteeNickname)
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .build();
        roomRepository.saveAll(List.of(room2, room3));

        // save messages
        // 주의: roomId가 room, room2, room3의 아이디와 동일해야 한다.
        LocalDateTime now = LocalDateTime.now();
        List<ChatMessage> saveThreeMessages = Arrays.asList(
                ChatMessage.builder()
                        ._id("test_uuid_1")
                        .roomId(TEST_ROOM_ID)
                        .senderType(SenderType.MENTEE)
                        .senderNickname(TEST_MENTEE_NICKNAME)
                        .message("test message 1")
                        .messageType(MessageType.TALK)
                        .time(now)
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_2")
                        .roomId(room2Id)
                        .senderType(SenderType.MENTEE)
                        .senderNickname(room2MenteeNickname)
                        .message(room2Msg)
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(3000))
                        .build(),
                ChatMessage.builder()
                        ._id("test_uuid_3")
                        .roomId(room3Id)
                        .senderType(SenderType.MENTOR)
                        .senderNickname(room3MenteeNickname)
                        .message(room3Msg)
                        .messageType(MessageType.TALK)
                        .time(now.plusSeconds(5000))
                        .build()
        );
        messageRepository.saveAll(saveThreeMessages);

        // when
        List<RoomListResponse> getRoomList = roomService.getAllRooms(TEST_MENTOR_NICKNAME, TYPE_MENTOR);

        // then
        // test if size is 3
        assertThat(getRoomList.size()).isEqualTo(3);

        // test if room2Msg, room3Msg contains correctly
        List<String> messageList = Arrays.asList(getRoomList.get(0).getLastMessage(),
                getRoomList.get(1).getLastMessage(), getRoomList.get(2).getLastMessage());
        boolean room2MsgExists = messageList.stream().anyMatch(
                msg -> msg.equals(room2Msg));
        boolean room3MsgExists = messageList.stream().anyMatch(
                msg -> msg.equals(room2Msg));
        assertThat(room2MsgExists).isTrue();
        assertThat(room3MsgExists).isTrue();
    }

    @Test
    @DisplayName("멘토가 멘토링 페이지를 조회하였으나, 데이터가 없는 경우 size가 0이다")
    void getAllRooms_By_MENTOR_when_data_is_Null() {
        String notExistsMentorNickname = "mentor_haha_hoho";
        List<RoomListResponse> getRoomList = roomService.getAllRooms(notExistsMentorNickname, TYPE_MENTOR);

        assertThat(getRoomList.size()).isZero();
    }

    @Test
    @DisplayName("type이 MENTEE, MENTOR가 아닌 경우 CustomException 리턴")
    void getAllRooms_type_mismatch() {
        String typeMismatch = "MENTORWA";
        assertThrows(CustomException.class, () -> roomService.getAllRooms(TEST_MENTEE_NICKNAME, typeMismatch));
    }
}