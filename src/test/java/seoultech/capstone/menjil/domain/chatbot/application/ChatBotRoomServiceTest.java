package seoultech.capstone.menjil.domain.chatbot.application;

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
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.ChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.DeleteChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.domain.chatbot.dao.ChatBotRoomRepository;
import seoultech.capstone.menjil.domain.chatbot.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;
import seoultech.capstone.menjil.domain.chatbot.domain.SenderType;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ChatBotRoomServiceTest {

    @Autowired
    private ChatBotRoomService chatBotRoomService;

    @Autowired
    private ChatBotRoomRepository chatBotRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final String TEST_INITIATOR_NICKNAME = "test_initiator_user";
    private final String TEST_RECIPIENT_NICKNAME = "test_receiver_user";
    private String TEST_ROOM_ID;

    @BeforeEach
    void setUp() {
        // Save Chat-bot Room
        TEST_ROOM_ID = chatBotRoomService.createRoomId(TEST_INITIATOR_NICKNAME, TEST_RECIPIENT_NICKNAME);

        ChatBotRoom chatBotRoom = ChatBotRoom.builder()
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(TEST_RECIPIENT_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();
        chatBotRoomRepository.save(chatBotRoom);

        // 챗봇 서비스를 요청한 사용자와, 요청받은 사용자를 생성 및 저장
        User initiator = createUser("google_4321432143", "user1@google.com", TEST_INITIATOR_NICKNAME, "default/profile.png");
        User receiver = createUser("google_4321432144", "user2@google.com", TEST_RECIPIENT_NICKNAME, "default/profile.png");
        userRepository.saveAll(List.of(initiator, receiver));
    }

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
    }

    /**
     * enterChatBotRoom
     */
    @Test
    @DisplayName("case 1: 요청자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_initiator_Not_In_Db() {
        // given
        String initNick = "hello";
        String receiverNick = "hello2";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(initNick)
                .recipientNickname(receiverNick)
                .build();

        // when // then
        assertThrows(CustomException.class, () -> chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()));
    }

    @Test
    @DisplayName("case 1-1: 응답자의 정보가 db에 없는 경우, 방 입장 시에 예외발생")
    void enterChatBotRoom_receiverNick_Not_In_Db() {
        // given
        String receiverNick = "hello2";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(receiverNick)
                .build();

        // when // then
        assertThrows(CustomException.class, () -> chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest()));
    }

    @Test
    @DisplayName("case 2: 방 입장 요청시 채팅방이 존재하지 않는 경우, 채팅방 및 WelcomeMsg를 생성 한다. " +
            "그리고 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Not_Exists() {
        // given
        String user = "nickname33";
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(user)
                .build();

        // save receiver user data
        String imgUrl2 = "default/img_url";
        User receiver2 = createUser("google_9998887776", "room2@google.com", user, imgUrl2);
        userRepository.save(receiver2);

        // when
        ChatBotRoomIdResponse response = chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest());

        // then
        // 방 Id 생성 검증
        String chatBotRoomId = response.getChatBotRoomId();
        assertThat(chatBotRoomId).isEqualTo(chatBotRoomService.createRoomId(TEST_INITIATOR_NICKNAME, user));

        // Welcome Message 생성 검증
        assertThat(messageRepository.findByRoomId(chatBotRoomId)).isNotNull();

        assertThat(messageRepository.findByRoomId(chatBotRoomId).get().getRoomId()).isEqualTo(chatBotRoomId);
        assertThat(messageRepository.findByRoomId(chatBotRoomId).get().get_id()).isNotNull();
        assertThat(messageRepository.findByRoomId(chatBotRoomId).get().getMessage()).isNotNull();
        assertThat(messageRepository.findByRoomId(chatBotRoomId).get().getSenderType()).isEqualTo(SenderType.AI);
    }

    @Test
    @DisplayName("case 2-1: 방 입장 요청시 채팅방이 이미 존재하는 경우 채팅방 Id를 리턴한다")
    void enterChatBotRoom_Room_Existed() {
        // given
        ChatBotRoomRequest roomDto = ChatBotRoomRequest.builder()
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(TEST_RECIPIENT_NICKNAME)
                .build();

        // when
        ChatBotRoomIdResponse response = chatBotRoomService.enterChatBotRoom(roomDto.toServiceRequest());

        // then
        String chatBotRoomId = response.getChatBotRoomId();
        assertThat(chatBotRoomId).isEqualTo(chatBotRoomService.createRoomId(TEST_INITIATOR_NICKNAME,
                TEST_RECIPIENT_NICKNAME));
    }

    /**
     * createRoomId
     */
    @Test
    @DisplayName("같은 변수에 대해 UUID 값이 일치하는지 검증한다")
    void createRoomId() {
        // given
        String val1 = "hello";
        String val2 = "InsideTheLines";
        String val3 = "insideTheLines"; // 첫 글자 알파벳 변경

        // when
        String uuid1 = chatBotRoomService.createRoomId(val1, val2);
        String uuid2 = chatBotRoomService.createRoomId(val1, val2);
        String uuid3 = chatBotRoomService.createRoomId(val1, val3);
        String reversedUuid1 = chatBotRoomService.createRoomId(val2, val1);

        // then
        assertThat(uuid1).isEqualTo(uuid2);
        assertThat(uuid1).isNotEqualTo(uuid3);
        assertThat(uuid1).isNotEqualTo(reversedUuid1);
    }

    /*

    @Test
    @DisplayName("case 2: 방 입장시 채팅방이 db에 존재하지 않는 경우, Welcome Message를 보내준다")
    void enterChatBotRoom_Room_Not_Exists() {
        // given
        String roomId = TEST_ROOM_ID + "no";

        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(roomId)
                .build();

        // when
        List<MessageOrderResponse> messageOrderResponses = roomService.enterChatBotRoom(roomDto.toServiceRequest());

        // then
        assertThat(messageOrderResponses.size()).isEqualTo(1);

        MessageOrderResponse response = messageOrderResponses.get(0);

        assertThat(response.getRoomId()).isEqualTo(roomId);
        assertThat(response.getSenderNickname()).isEqualTo(TEST_MENTOR_NICKNAME); // Welcome Message is sent by mentor
        assertThat(response.getSenderType()).isEqualTo(SenderType.MENTOR);
        assertThat(response.getMessageType()).isEqualTo(MessageType.ENTER);
    }

    @Test
    @DisplayName("case 3 : 방 입장시 채팅방이 db에 존재하는 경우, db에 저장된 메시지들을 응답으로 보낸다: 메시지가 3개 존재하는 경우")
    void enterChatBotRoom_when_Room_already_exists() {
        RoomDto roomDto = RoomDto.builder()
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

        // when
        List<MessageOrderResponse> messageOrderResponses = roomService.enterChatBotRoom(roomDto.toServiceRequest());

        // then
        assertThat(messageOrderResponses.size()).isEqualTo(3);

        // 대화는 챗봇 형식, 즉 일대일로 진행되므로, 멘티와 멘토 타입이 존재할 수밖에 없다.
        List<SenderType> senderTypesList = Arrays.asList(messageOrderResponses.get(0).getSenderType(),
                messageOrderResponses.get(1).getSenderType(), messageOrderResponses.get(2).getSenderType());
        boolean menteeExists = senderTypesList.stream().anyMatch(
                type -> type.equals(SenderType.MENTEE));
        boolean mentorExists = senderTypesList.stream().anyMatch(
                type -> type.equals(SenderType.MENTOR));
        assertThat(menteeExists).isTrue();
        assertThat(mentorExists).isTrue();

        // Order 1, 2, 3이 정상적으로 리턴되는지 확인
        List<Integer> orderList = Arrays.asList(messageOrderResponses.get(0).getOrder(), messageOrderResponses.get(1).getOrder(),
                messageOrderResponses.get(2).getOrder());
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

        // 가장 나중에 작성된, 즉 시간이 가장 나중인 메시지가 order=3인지 확인
        MessageOrderResponse firstMsg = messageOrderResponses.get(0);
        assertThat(firstMsg.getOrder()).isEqualTo(3);
        assertThat(firstMsg.get_id()).isEqualTo("test_uuid_3");

        // 원활한 비교를 위해 milliseconds 0으로 설정
        assertThat(firstMsg.getTime()).isAfterOrEqualTo(now.plusSeconds(5000).withNano(0));
    }

    @Test
    @DisplayName("case 3-1 : 방 입장시 채팅방이 db에 존재하는 경우, db에 저장된 메시지들을 응답으로 보낸다: 메시지가 다수 존재하는 경우")
    void enterChatBotRoom_when_Room_already_exists_2() {
        // given
        RoomDto roomDto = RoomDto.builder()
                .mentorNickname(TEST_MENTOR_NICKNAME)
                .menteeNickname(TEST_MENTEE_NICKNAME)
                .roomId(TEST_ROOM_ID)
                .build();
        LocalDateTime now = LocalDateTime.now();
        int FIXED_NUM = 76;

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
                    .roomId(TEST_ROOM_ID)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        List<MessageOrderResponse> messageOrderResponses = roomService.enterChatBotRoom(roomDto.toServiceRequest());

        // then
        assertThat(messageOrderResponses.size()).isEqualTo(10);

        MessageOrderResponse lastResponse = messageOrderResponses.get(0); // 불러온 10개의 대화 중, 가장 마지막 대화내용
        assertThat(lastResponse.get_id()).isEqualTo("id_" + FIXED_NUM);

        MessageOrderResponse firstResponse = messageOrderResponses.get(messageOrderResponses.size() - 1); // 불러온 10개의 대화 중, 첫 번째 대화내용
        assertThat(firstResponse.get_id()).isEqualTo("id_" + (FIXED_NUM - 10 + 1));
    } */

    /**
     * getAllChatBotRooms
     */
    @Test
    @DisplayName("case 1: 기존 챗봇 대화방이 1개 존재하는 경우")
    void getAllChatBotRooms_Number_of_Room_Is_One() {
        // given // when
        List<ChatBotRoomResponse> getChatBotRooms = chatBotRoomService.getAllChatBotRooms(TEST_INITIATOR_NICKNAME);

        // then
        assertThat(getChatBotRooms.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("case 1-1: 기존 챗봇 대화방이 2개 존재하는 경우")
    void getAllChatBotRooms_Number_Of_Room_Is_Two() {
        // given
        String room2Id = "room2";
        String room2RecipientNickname = TEST_RECIPIENT_NICKNAME + "room2";

        // save chatbot room
        ChatBotRoom room2 = ChatBotRoom.builder()
                .roomId(room2Id)
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(room2RecipientNickname)
                .build();
        chatBotRoomRepository.save(room2);

        // save receiver user data
        String imgUrl2 = "default/img_url_222";
        User receiver2 = createUser("google_9998887776", "room2@google.com", room2RecipientNickname, imgUrl2);
        userRepository.save(receiver2);

        // when
        List<ChatBotRoomResponse> chatBotRooms = chatBotRoomService.getAllChatBotRooms(TEST_INITIATOR_NICKNAME);

        // then
        // test if size is 2
        assertThat(chatBotRooms.size()).isEqualTo(2);

        // Recipient nickname 정보가 잘 담겨있는지 검증
        List<String> receivers = Arrays.asList(chatBotRooms.get(0).getRecipientNickname(),
                chatBotRooms.get(1).getRecipientNickname());
        boolean ChatBotRoomResponseContainsReceiver1 = receivers.stream().anyMatch(msg -> msg.equals(TEST_RECIPIENT_NICKNAME));
        boolean ChatBotRoomResponseContainsReceiver2 = receivers.stream().anyMatch(msg -> msg.equals(room2RecipientNickname));
        assertThat(ChatBotRoomResponseContainsReceiver1).isTrue();
        assertThat(ChatBotRoomResponseContainsReceiver2).isTrue();

        // img_url 정보가 잘 담기는지 검증
        assertThat(chatBotRooms.get(0).getImgUrl()).isNotNull();
        assertThat(chatBotRooms.get(1).getImgUrl()).isNotNull();
    }

    @Test
    @DisplayName("case 2: 기존에 챗봇 대화방이 하나도 존재하지 않은 경우")
    void getAllChatBotRooms_ChatBotRoom_Not_Existed() {
        // given
        String nickname = "testUser";
        List<ChatBotRoomResponse> getRoomList = chatBotRoomService.getAllChatBotRooms(nickname);

        // when // then
        assertThat(getRoomList.isEmpty()).isTrue();
    }

    /**
     * quitRoom
     */
    @Test
    @DisplayName("채팅방과 채팅방에 포함된 대화 메시지가 정상적으로 제거되는지 검증")
    void quitRoom_success() {
        // given
        DeleteChatBotRoomRequest deleteChatBotRoomRequest = DeleteChatBotRoomRequest
                .builder()
                .roomId(TEST_ROOM_ID)
                .initiatorNickname(TEST_INITIATOR_NICKNAME)
                .recipientNickname(TEST_RECIPIENT_NICKNAME)
                .build();

        LocalDateTime now = LocalDateTime.now();
        int FIXED_NUM = 9;
        List<ChatMessage> chatMessageList = new ArrayList<>();
        for (int i = 1; i <= FIXED_NUM; i++) {
            String _id = "id_" + i;
            SenderType senderType;
            String senderNickname;
            if (i % 2 == 0) {
                senderType = SenderType.AI;
                senderNickname = TEST_RECIPIENT_NICKNAME;
            } else {
                senderType = SenderType.USER;
                senderNickname = TEST_INITIATOR_NICKNAME;
            }
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time = now.plusSeconds(i * 1000L);

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(TEST_ROOM_ID)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        boolean result = chatBotRoomService.quitRoom(deleteChatBotRoomRequest.toServiceRequest());

        // then
        assertThat(result).isTrue();
        assertThat(messageRepository.findAll().size()).isZero();    // Delete Messages
        assertThat(chatBotRoomRepository.findAll().size()).isZero(); // Delete chatbot room
    }

    private User createUser(String id, String email, String nickname, String imgUrl) {
        return User.builder()
                .id(id).email(email).provider("google").nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .imgUrl(imgUrl)  // set img url
                .build();
    }
}