package seoultech.capstone.menjil.domain.chat.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.request.AwsLambdaRequest;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.AwsLambdaResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageListResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class MessageService {

    private final WebClient flaskWebClient;
    private final ChatGptService chatGptService;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public MessageService(@Qualifier("flaskWebClient") WebClient flaskWebClient,
                          ChatGptService chatGptService, MessageRepository messageRepository,
                          RoomRepository roomRepository) {
        this.flaskWebClient = flaskWebClient;
        this.chatGptService = chatGptService;
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * 사용자에게 첫 응답 메시지를 보낸다.
     */
    public MessageResponse sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String welcomeMessage = "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";

        LocalDateTime now = LocalDateTime.now().withNano(0);     // ignore milliseconds
        welcomeMsg.setWelcomeMessage(roomDto.getRoomId(), SenderType.MENTOR,
                roomDto.getMentorNickname(), welcomeMessage, MessageType.ENTER, now);

        // save entity to mongoDB
        try {
            messageRepository.save(welcomeMsg);
        } catch (RuntimeException e) {
            log.error(">> messageRepository.save() error occured ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        // Entity -> Dto
        return MessageResponse.fromChatMessageEntity(welcomeMsg, null);
    }

    /**
     * 채팅 메시지를 저장한다.
     */
    public boolean saveChatMessage(MessageRequest messageRequest) {
        // messageDto의 time format 검증
        LocalDateTime dateTime;
        try {
            String time = messageRequest.getTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTime = LocalDateTime.parse(time, formatter);
        } catch (RuntimeException e) {
            log.error(">> Failed to parse date-time string.", e);
            throw new CustomException(ErrorCode.TIME_INPUT_INVALID);
        }

        // MessageRequest -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = MessageRequest.toChatMessageEntity(messageRequest, dateTime);

        // save entity to mongoDB
        // 저장이 잘된 경우 true, 그렇지 않은 경우 false 리턴
        try {
            messageRepository.save(chatMessage);
        } catch (RuntimeException e) {
            log.error(">> messageRepository.save() error occured ", e);
            return false;
        }
        return true;
    }

    /**
     * MessageType: QUESTION
     */
    public MessageResponse sendAIMessage(String roomId, MessageRequest messageRequest) {
        String specificMessage = "당신의 궁금증을 빠르게 해결할 수 있게 도와줄 AI 서포터입니다.\n" +
                "멘토의 답변을 기다리면서, 당신의 질문과 유사한 질문에서 시작된 대화를 살펴보실래요?\n" +
                "더 신속하게, 다양한 해답을 얻을 수도 있을 거예요!";

        LocalDateTime now = LocalDateTime.now().withNano(0);     // ignore milliseconds
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(findMentorNickname(roomId, messageRequest.getSenderNickname()))
                .message(specificMessage)
                .messageType(MessageType.AI_QUESTION_RESPONSE)
                .time(now)
                .build();
        try {
            messageRepository.save(message);
        } catch (RuntimeException e) {
            log.error(">> sendAIMessage] save() error occured ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        return MessageResponse.fromChatMessageEntity(message, null);
    }

    public MessageListResponse handleQuestion(String roomId, MessageRequest messageRequest) {
        // 1. 사용자가 입력한 채팅 메시지 저장
        boolean saveMsg = saveChatMessage(messageRequest);
        if (!saveMsg) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
        String mentorNickname = findMentorNickname(roomId, messageRequest.getSenderNickname());

        // 2. ChatGPT에게 질문 데이터 전달하여 세줄 요약 결과를 받아온다.
        Message message = chatGptService.getMessageFromGpt(messageRequest.getMessage());

        // 3. Create AwsLambdaRequest
        AwsLambdaRequest awsLambdaRequest = AwsLambdaRequest.of(messageRequest.getSenderNickname(),
                mentorNickname, messageRequest.getMessage(), message.getContent());

        // 4. Make the POST request to AWS Lambda and block to get the response
        List<AwsLambdaResponse> awsLambdaResponseList = flaskWebClient.post()
                .uri("/api/lambda/question")
                .body(BodyInserters.fromValue(awsLambdaRequest))
                .retrieve()
                .bodyToFlux(AwsLambdaResponse.class)
                .collectList()
                .block();  // Use block() for a non-reactive application*/

        // 5. 응답 메시지 db에 저장
        LocalDateTime now = LocalDateTime.now().withNano(0);     // ignore milliseconds
        ChatMessage flaskResponseMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(mentorNickname)
                .messageList(awsLambdaResponseList)  // save three of summary_question and answer
                .messageType(MessageType.AI_SUMMARY)
                .time(now)
                .build();

        try {
            messageRepository.save(flaskResponseMessage);
        } catch (RuntimeException e) {
            log.error(">> handleQuestion] save() error occured ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        return MessageListResponse.fromChatMessageEntity(flaskResponseMessage, null);
    }

    /**
     * 멘토의 닉네임을 가져오는 메서드
     * 방 id로 채팅 내역을 조회한 다음, messageType: ENTER 인 값을 찾는다.
     */
    // 그런데 RoomRepository에서, roomId와 menteeNickname으로 조회하는게 더 낫지 않을까? 추후 생각해보기
    public String findMentorNickname(String roomId, String menteeNickname) {
//        ChatMessage message = messageRepository.findChatMessageByRoomIdAndMessageType(roomId, MessageType.ENTER);
//        return message.getSenderNickname();
        Room room = roomRepository.findRoomByIdAndMenteeNickname(roomId, menteeNickname);
        return room.getMentorNickname();
    }
}
