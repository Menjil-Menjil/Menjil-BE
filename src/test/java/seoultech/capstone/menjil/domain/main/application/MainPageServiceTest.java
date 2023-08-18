package seoultech.capstone.menjil.domain.main.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfo;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfo;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MainPageServiceTest {

    @Autowired
    private MainPageService mainPageService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MessageRepository messageRepository;

    private final String TEST_MENTEE_NICKNAME = "test_mentee_nickname";
    private final String TEST_MENTOR_NICKNAME = "test_mentor_nickname";

    @BeforeEach
    void setUp() {
        User userA = createUser("google_1231323", "test@google.com", TEST_MENTEE_NICKNAME, UserRole.MENTEE);
        User userB = createUser("google_1231324", "test2@google.com", TEST_MENTOR_NICKNAME, UserRole.MENTOR);
        userRepository.saveAll(List.of(userA, userB));
    }

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
    }

    /**
     * getUserInfo()
     */
    @Test
    @DisplayName("db에 닉네임이 존재하는 경우 정상적으로 UserInfo 객체 리턴")
    void getUserInfo() {
        // given
        String nickname = TEST_MENTEE_NICKNAME;

        // when
        UserInfo userInfo = mainPageService.getUserInfo(nickname);

        // then
        assertThat(userInfo.getNickname()).isEqualTo(TEST_MENTEE_NICKNAME);
        assertThat(userInfo.getSchool()).isEqualTo("서울과학기술대학교");
        assertThat(userInfo.getMajor()).isEqualTo("컴퓨터공학과");
    }

    @Test
    @DisplayName("db에 닉네임이 존재하지 않는 경우 CustomException 리턴")
    void getUserInfo_nickname_not_exists_in_db() {
        // given
        String nickname = "nickname_not_in_db";

        // when
        assertThrows(CustomException.class, () -> mainPageService.getUserInfo(nickname));
    }


    /**
     * getUserRoomList()
     */
    @Test
    @DisplayName("사용자의 닉네임이 db에 없는 경우 CustomException 리턴")
    void getUserRoomList_when_nickname_not_exists_in_db() {
        // given
        String menteeNickname = "mentee_nickname_not_exists_in_db";

        // when
        assertThrows(CustomException.class, () -> mainPageService.getUserRoomList(menteeNickname));
    }

    @Test
    @DisplayName("'멘티'의 닉네임이 db에 존재하지만 채팅방이 하나도 없는 경우 empty List 리턴")
    void getUserRoomList_when_mentee_room_not_exists_in_db() {
        // given
        String menteeNickname = TEST_MENTEE_NICKNAME;

        // when
        List<RoomInfo> result = mainPageService.getUserRoomList(menteeNickname);

        // then
        assertThat(result.size()).isZero();
    }

    @Test
    @DisplayName("'멘토'의 닉네임이 db에 존재하지만 채팅방이 하나도 없는 경우 empty List 리턴")
    void getUserRoomList_when_mentor_oom_not_exists_in_db() {
        // given
        String mentorNickname = TEST_MENTOR_NICKNAME;

        // when
        List<RoomInfo> result = mainPageService.getUserRoomList(mentorNickname);

        // then
        assertThat(result.size()).isZero();
    }

    @Test
    @DisplayName("'멘티'의 닉네임에 대한 방이 하나 존재, 방 메시지가 13개 존재하는 경우, 방 1개에 대한 가장 마지막에 작성된 메시지가 반환된다")
    void getUserRoomList_of_mentee_when_room_isOne_and_messages_is_13() {
        // given
        String menteeNickname = TEST_MENTEE_NICKNAME;
        String roomId = "test_room_1";

        // Create and Save One Room
        Room room = Room.builder()
                .roomId(roomId)
                .menteeNickname(menteeNickname)    // here is menteenickname
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .build();
        roomRepository.save(room);

        // Create 13 Chat Messages and Save
        int FIXED_NUM = 13;
        LocalDateTime now = LocalDateTime.now().withNano(0);    // remove milliseconds
        List<ChatMessage> chatMessageList = new ArrayList<>();

        for (int i = 1; i <= FIXED_NUM; i++) {
            String _id = "id_" + i;
            SenderType senderType;
            String senderNickname;
            if (i % 2 == 0) {
                senderType = SenderType.MENTOR;
                senderNickname = TEST_MENTOR_NICKNAME;
            } else {
                senderType = SenderType.MENTEE;
                senderNickname = TEST_MENTEE_NICKNAME;
            }
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time = now.plusSeconds(i * 1000L);

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(roomId)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        List<RoomInfo> result = mainPageService.getUserRoomList(menteeNickname);    // here is menteenickname

        // then
        assertThat(result.size()).isEqualTo(1);

        RoomInfo oneRoom = result.get(0);
        assertThat(oneRoom.getRoomId()).isEqualTo(roomId);
        assertThat(oneRoom.getLastMessage()).isEqualTo("message_" + FIXED_NUM);
        assertThat(oneRoom.getLastMessageTime()).isEqualTo(now.plusSeconds(FIXED_NUM * 1000L));
    }

    @Test
    @DisplayName("'멘토'의 닉네임에 대한 방이 하나 존재, 방 메시지가 13개 존재하는 경우, 방 1개에 대한 가장 마지막에 작성된 메시지가 반환된다")
    void getUserRoomList_of_mentor_when_room_isOne_and_messages_is_13() {
        // given
        String mentorNickname = TEST_MENTOR_NICKNAME;
        String roomId = "test_room_1";

        // Create and Save One Room
        Room room = Room.builder()
                .roomId(roomId)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .mentorNickname(mentorNickname)    // here is mentornickname
                .build();
        roomRepository.save(room);

        // Create 13 Chat Messages and Save
        int FIXED_NUM = 13;
        LocalDateTime now = LocalDateTime.now().withNano(0);    // remove milliseconds
        List<ChatMessage> chatMessageList = new ArrayList<>();

        for (int i = 1; i <= FIXED_NUM; i++) {
            String _id = "id_" + i;
            SenderType senderType;
            String senderNickname;
            if (i % 2 == 0) {
                senderType = SenderType.MENTOR;
                senderNickname = TEST_MENTOR_NICKNAME;
            } else {
                senderType = SenderType.MENTEE;
                senderNickname = TEST_MENTEE_NICKNAME;
            }
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time = now.plusSeconds(i * 1000L);

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(roomId)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        List<RoomInfo> result = mainPageService.getUserRoomList(mentorNickname);    // here is mentornickname

        // then
        assertThat(result.size()).isEqualTo(1);

        RoomInfo oneRoom = result.get(0);
        assertThat(oneRoom.getRoomId()).isEqualTo(roomId);
        assertThat(oneRoom.getLastMessage()).isEqualTo("message_" + FIXED_NUM);
        assertThat(oneRoom.getLastMessageTime()).isEqualTo(now.plusSeconds(FIXED_NUM * 1000L));
    }

    /* 이 경우는 '멘티'의 경우만 검증해도 '멘토'의 경우도 함께 검증할 수 있을 것으로 판단하여,
    멘티의 경우만 테스트 코드를 작성함
     */
    @Test
    @DisplayName("'멘티'의 닉네임에 대한 방이 5개 존재, 방 메시지가 1개씩 존재하는 경우, " +
            "방 3개가 가장 마지막 메시지를 기준으로 내림차순 정렬이 되는지 검증")
    void getUserRoomList_of_mentee_does_room_is_order_by_DESC() {
        // given
        String menteeNickname = TEST_MENTEE_NICKNAME;
        String roomId = "test_room_";
        String mentorNickname = "test_test_mentor_";    // TEST_MENTOR_NICKNAME와 구분을 위해

        // Create and Save Five Room
        int ROOM_NUM = 5;
        List<Room> rooms = new ArrayList<>();
        for (int i = 1; i <= ROOM_NUM; i++) {
            rooms.add(Room.builder()
                    .roomId(roomId + i)
                    .menteeNickname(menteeNickname)    // here is menteenickname
                    .mentorNickname(mentorNickname + i)
                    .build());
        }
        roomRepository.saveAll(rooms);

        // 방이 존재하므로, 그에 대한 멘토 5명의 데이터도 users table에 미리 저장이 되어 있어야 한다.
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= ROOM_NUM; i++) {
            users.add(createUser("id_" + i, "testtt" + i + "@google.com",
                    mentorNickname + i, UserRole.MENTOR));
        }
        userRepository.saveAll(users);

        // 각 채팅방 별로 채팅 메시지를 저장. 메시지는 하나씩만 저장
        LocalDateTime now = LocalDateTime.now().withNano(0);    // remove milliseconds
        List<ChatMessage> chatMessageList = new ArrayList<>();

        for (int i = 1; i <= ROOM_NUM; i++) {
            String _id = "id_" + i;
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time = now.plusSeconds(i * 1000L);

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(roomId + i)
                    .senderType(SenderType.MENTOR)
                    .senderNickname(mentorNickname + i)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        List<RoomInfo> result = mainPageService.getUserRoomList(menteeNickname);    // here is menteenickname

        // then
        assertThat(result.size()).isEqualTo(5);

        // 리스트의 인덱스가 작을 수록, 가장 최근에 대화한 메시지가 존재하는 방이다.
        RoomInfo firstRoom = result.get(0);
        RoomInfo middleRoom = result.get((result.size() - 1) / 2);
        RoomInfo lastRoom = result.get(result.size() - 1);

        assertThat(firstRoom.getLastMessageTime()).isAfter(middleRoom.getLastMessageTime());
        assertThat(middleRoom.getLastMessageTime()).isAfter(lastRoom.getLastMessageTime());
    }


    private User createUser(String id, String email, String nickname, UserRole role) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .role(role).birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("컴퓨터공학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .optionInfo(null)
                .imgUrl("default/profile.png")  // set img url
                .build();
    }
}