//package seoultech.capstone.menjil.domain.chatbot.application;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
//import seoultech.capstone.menjil.domain.auth.domain.User;
//import seoultech.capstone.menjil.domain.chatbot.dao.MessageRepository;
//import seoultech.capstone.menjil.domain.chatbot.dao.RoomRepository;
//import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;
//import seoultech.capstone.menjil.domain.chatbot.domain.Room;
//import seoultech.capstone.menjil.domain.chatbot.domain.SenderType;
//import seoultech.capstone.menjil.domain.chatbot.dto.RoomDto;
//import seoultech.capstone.menjil.domain.chatbot.dto.request.MessageRequest;
//import seoultech.capstone.menjil.domain.chatbot.dto.response.MessageOrderResponse;
//import seoultech.capstone.menjil.domain.chatbot.dto.response.MessageResponse;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//@ActiveProfiles("test")
//class MessageServiceTest {
//
//    @Autowired
//    private MessageService messageService;
//    @Autowired
//    private RoomRepository roomRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private MessageRepository messageRepository;
//
//    private final static String TEST_ROOM_ID = "test_room_1";
//    private final String TEST_MENTEE_NICKNAME = "test_mentee_1";
//    private final String TEST_MENTOR_NICKNAME = "test_mentor_1";
//
//    @BeforeEach
//    void setUp() {
//        // Save Room
//        Room room = Room.builder()
//                .roomId(TEST_ROOM_ID)
//                .menteeNickname(TEST_MENTEE_NICKNAME)
//                .mentorNickname(TEST_MENTOR_NICKNAME)
//                .build();
//        roomRepository.save(room);
//
//        // Save Mentee and Mentor
//        User mentee = createUser("google_123123", "mentee@mentee.com", TEST_MENTEE_NICKNAME );
//        User mentor = createUser("google_1231234", "mentor@mentor.com", TEST_MENTOR_NICKNAME );
//        userRepository.saveAll(List.of(mentee, mentor));
//    }
//
//    @AfterEach
//    void resetData() {
//        // delete mongodb manually
//        messageRepository.deleteAll();
//    }
//
//    /**
//     * sendWelcomeMessage
//     */
//    @Test
//    @DisplayName("정상적으로 응답 메시지가 생성되며, MessageOrderResponse Dto 객체가 리턴된다")
//    void sendWelcomeMessage() {
//        // given
//        String roomId = "room_id_one";
//        String menteeNickname = "test_mentee_one";
//        String mentorNickname = "test_mentor_one";
//
//        RoomDto roomDto = RoomDto.builder()
//                .menteeNickname(menteeNickname)
//                .mentorNickname(mentorNickname)
//                .roomId(roomId)
//                .build();
//
//        // when
//        MessageOrderResponse result = messageService.sendWelcomeMessage(roomDto).orElse(null);
//
//        // then
//        assert result != null;
//        assertThat(result.getRoomId()).isEqualTo(roomId);
//        assertThat(result.getOrder()).isNull();
//        assertThat(result.getSenderType()).isEqualTo(SenderType.MENTOR);
//        assertThat(result.getSenderNickname()).isEqualTo(mentorNickname);
//        assertThat(result.getMessage()).isNotBlank();   // check that a string is not null, not empty, and not just whitespace.
//        assertThat(result.getMessageType()).isEqualTo(MessageType.ENTER);
//    }
//
//    // TODO: 서비스 로직에서 제외되면 제거할 것
////    /**
////     * saveChatMessage
////     */
////    @Test
////    @DisplayName("case SAVE_SUCCESS: 클라이언트로 들어오는 채팅 메시지가 db에 저장이 정상적으로 되는 경우 int 0 리턴")
////    void saveChatMessage_SAVE_SUCCESS() {
////        // given
////        String formattedDateTime = createTimeFormatOfMessageResponse(LocalDateTime.now());
////
////        MessageRequest messageDto = MessageRequest.builder()
////                .roomId("test_room_3")
////                .senderType(SenderType.MENTOR)
////                .senderNickname("test_mentor_nickname")
////                .message("Welcome Message")
////                .messageType(MessageType.ENTER)
////                .time(formattedDateTime)
////                .build();
////
////        // when
////        int result = messageService.saveChatMessage(messageDto);
////
////        // then
////        assertThat(result).isEqualTo(SUCCESS.getValue());
////    }
////
////    @Test
////    @DisplayName("case TIME_INPUT_INVALID: MessageDto에서 time 형식이 올바르지 않은 경우 int -1 리턴")
////    void saveChatMessage_TIME_INPUT_INVALID() {
////        // given
////        String format_is_wrong = "2023:08:14T12:23:00";
////        String format_is_wrong_v2 = "2023:08:14  12:23:00"; // 공백 2칸
////
////        MessageRequest messageDto1 = MessageRequest.builder()
////                .roomId("test_room_3")
////                .senderType(SenderType.MENTOR)
////                .senderNickname("test_mentor_nickname")
////                .message("Welcome Message")
////                .messageType(MessageType.ENTER)
////                .time(format_is_wrong)
////                .build();
////
////        MessageRequest messageDto2 = MessageRequest.builder()
////                .roomId("test_room_3")
////                .senderType(SenderType.MENTOR)
////                .senderNickname("test_mentor_nickname")
////                .message("Welcome Message")
////                .messageType(MessageType.ENTER)
////                .time(format_is_wrong_v2)
////                .build();
////
////        // when
////        int result1 = messageService.saveChatMessage(messageDto1);
////        int result2 = messageService.saveChatMessage(messageDto2);
////
////        // then
////        assertThat(result1).isEqualTo(TIME_INPUT_INVALID.getValue());
////        assertThat(result2).isEqualTo(TIME_INPUT_INVALID.getValue());
////    }
////
////    /**
////     * sendClientMessage
////     */
////    @Test
////    @DisplayName("정상적인 로직이 수행됨: 멘티의 질문을 그대로 전달한다")
////    void sendClientMessage() {
////        // given
////        String roomId = "test_room_3";
////        String formattedDateTime = createTimeFormatOfMessageResponse(LocalDateTime.now());
////
////        MessageRequest clientMessageDto = MessageRequest.builder()
////                .roomId(roomId)
////                .senderType(SenderType.MENTEE)
////                .senderNickname("test_mentee_nickname")
////                .message("멘티의 질문입니다")
////                .messageType(MessageType.C_QUESTION)
////                .time(formattedDateTime)
////                .build();
////
////        // when
////        MessageResponse clientResponse = messageService.sendClientMessage(clientMessageDto);
////
////        // then
////        assertThat(clientResponse).isNotNull();
////        assertThat(clientResponse.getRoomId()).isEqualTo(roomId);
////        assertThat(clientResponse.getMessageType()).isEqualTo(MessageType.C_QUESTION);
////    }
////
////    @Test
////    @DisplayName("saveChatMessage에서 먼저 검증을 하겠지만, 한 번 더 검증한다. 날자 형식이 맞지 않는 경우에는 null 리턴")
////    void sendClientMessage_when_time_format_wrong() {
////        // given
////        String wrongTimeString = "1996-12-01T00:04:27";
////
////        MessageRequest clientMessageDto = MessageRequest.builder()
////                .roomId("test_room_3")
////                .senderType(SenderType.MENTEE)
////                .senderNickname("test_mentee_nickname")
////                .message("멘티의 질문입니다")
////                .messageType(MessageType.C_QUESTION)
////                .time(wrongTimeString)
////                .build();
////
////        // when
////        MessageResponse clientResponse = messageService.sendClientMessage(clientMessageDto);
////
////        // then
////        assertThat(clientResponse).isNull();
////    }
//
//    /**
//     * sendAIMessage
//     */
//    @Test
//    @DisplayName("정상적인 로직이 수행됨: AI의 첫 응답이 그대로 전달된다")
//    void sendAIMessage() {
//        // given
//        String formattedDateTime = createTimeFormatOfMessageResponse(LocalDateTime.now());
//        String specificMessage = "당신의 궁금증을 빠르게 해결할 수 있게 도와드릴게요!";
//
//        // 여기 메시지는, sendClientMessage 에서 사용된 것과 동일한, 즉 클라이언트에서 보낸 메시지이다.
//        MessageRequest clientMessageDto = MessageRequest.builder()
//                .roomId(TEST_ROOM_ID)
//                .senderType(SenderType.MENTEE)
//                .senderNickname(TEST_MENTEE_NICKNAME)
//                .message("멘티의 질문입니다")
//                .messageType(MessageType.C_QUESTION)
//                .time(formattedDateTime)
//                .build();
//
//        // when
//        // 응답에서 AI 메시지가 보내지는 것임.
//        MessageResponse aiFirstResponse = messageService.sendAIMessage(TEST_ROOM_ID, clientMessageDto);
//
//        // then
//        assertThat(aiFirstResponse).isNotNull();
//        assertThat(aiFirstResponse.getMessageType()).isEqualTo(MessageType.AI_QUESTION_RESPONSE);
//        assertThat(aiFirstResponse.getMessage()).isEqualTo(specificMessage);
//    }
//
//    /**
//     * parseDateTime
//     */
//    @Test
//    @DisplayName("case success: yyyy-MM-dd HH:mm:ss 형태로 파싱이 잘 된 경우")
//    void parseDateTime() {
//        // given
//        String timeString = "1996-12-01 00:04:27";
//
//        // when
//        Optional<LocalDateTime> time = messageService.parseDateTime(timeString);
//
//        // then
//        assertThat(time).isPresent();
//
//        // 작거나 같고, 크거나 같은 두 조건을 만족하는 경우는 같은 경우 뿐이다.
//        assertThat(LocalDateTime.of(1996, 12, 1, 0, 4, 27))
//                .isBeforeOrEqualTo(time.get());
//        assertThat(LocalDateTime.of(1996, 12, 1, 0, 4, 27))
//                .isAfterOrEqualTo(time.get());
//    }
//
//    @Test
//    @DisplayName("case fail: yyyy-MM-dd HH:mm:ss 형태로 파싱이 잘 '안'된 경우")
//    void parseDateTime_fail() {
//        // given
//        String wrongTimeString1 = "1996-12-01T00:04:27";  // 사이에 'T' 존재
//        String wrongTimeString2 = "1996-12-01  00:04:27"; // 공백이 2개
//
//        // when
//        Optional<LocalDateTime> time1 = messageService.parseDateTime(wrongTimeString1);
//        Optional<LocalDateTime> time2 = messageService.parseDateTime(wrongTimeString2);
//
//        // then
//        assertThat(time1).isEmpty();
//        assertThat(time2).isEmpty();
//    }
//
//    private String createTimeFormatOfMessageResponse(LocalDateTime time) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return time.format(formatter);
//    }
//
//    private User createUser(String id, String email, String nickname) {
//        return User.builder()
//                .id(id).email(email).provider("google").nickname(nickname)
//                .birthYear(2000).birthMonth(3)
//                .school("서울과학기술대학교").score(3).scoreRange("중반")
//                .graduateDate(2021).graduateMonth(3)
//                .major("경제학과").subMajor(null)
//                .minor(null).field("백엔드").techStack("AWS")
//                .career(null)
//                .certificate(null)
//                .awards(null)
//                .activity(null)
//                .imgUrl("default/profile.png")  // set img url
//                .build();
//    }
//}