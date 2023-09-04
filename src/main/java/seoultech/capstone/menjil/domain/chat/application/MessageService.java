package seoultech.capstone.menjil.domain.chat.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.request.AwsLambdaRequest;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.AwsLambdaResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class MessageService {

    private final WebClient apiGatewayClient;
    private final ChatGptService chatGptService;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public MessageService(@Qualifier("apiGatewayClient") WebClient apiGatewayClient,
                          ChatGptService chatGptService, MessageRepository messageRepository,
                          RoomRepository roomRepository) {
        this.apiGatewayClient = apiGatewayClient;
        this.chatGptService = chatGptService;
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * 사용자에게 첫 응답 메시지를 보낸다.
     */
    public MessageResponse sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String roomId = roomDto.getRoomId();
        SenderType type = SenderType.MENTOR;
        String mentorNickname = roomDto.getMentorNickname();
        String welcomeMessage = "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";
        Object messageList = null;
        MessageType messageType = MessageType.ENTER;
        LocalDateTime now = LocalDateTime.now().withNano(0);     // ignore milliseconds

        // Create Welcome Message
        welcomeMsg.setWelcomeMessage(roomId, type, mentorNickname,
                welcomeMessage, messageList, messageType, now);

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
    public int saveChatMessage(MessageRequest messageRequest) {
        int TIME_INPUT_INVALID = 0;
        int INTERNAL_SERVER_ERROR = 1;
        int SAVE_SUCCESS = 100;

        // messageDto의 time format 검증
        LocalDateTime dateTime;
        try {
            String time = messageRequest.getTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTime = LocalDateTime.parse(time, formatter);
        } catch (RuntimeException e) {
            log.error(">> Failed to parse date-time string.", e);
            return TIME_INPUT_INVALID;
        }

        // MessageRequest -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = MessageRequest.toChatMessageEntity(messageRequest, dateTime);

        // save entity to mongoDB
        try {
            messageRepository.save(chatMessage);
        } catch (RuntimeException e) {
            log.error(">> messageRepository.save() error occured ", e);
            return INTERNAL_SERVER_ERROR;
        }
        return SAVE_SUCCESS;
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
            return null;
        }

        return MessageResponse.fromChatMessageEntity(message, null);
    }

    public MessageResponse handleQuestion(String roomId, MessageRequest messageRequest) {
        String mentorNickname = findMentorNickname(roomId, messageRequest.getSenderNickname());

        // 1. ChatGPT에게 질문 데이터 전달하여 세줄 요약 결과를 받아온다.
        Message message = chatGptService.getMessageFromGpt(messageRequest.getMessage());

        // 2. Create AwsLambdaRequest
        AwsLambdaRequest awsLambdaRequest = AwsLambdaRequest.of(messageRequest.getSenderNickname(),
                mentorNickname, messageRequest.getMessage(), message.getContent());

        // 3. Make the POST request to AWS Lambda and block to get the response
        List<AwsLambdaResponse> awsLambdaResponseList = null;
        try {
            awsLambdaResponseList = apiGatewayClient.post()
                    .uri("/api/lambda/question")
                    .body(BodyInserters.fromValue(awsLambdaRequest))
                    .retrieve()
                    .bodyToFlux(AwsLambdaResponse.class)
                    .timeout(Duration.ofSeconds(180))  // throws TimeoutException if no items are emitted within 180 seconds
                    .onErrorResume(e -> {
                        if (e instanceof java.util.concurrent.TimeoutException) {
                            log.error("Request to Lambda timed out", e);
                            return Mono.empty();
                        } else {
                            log.error("An error occurred", e);
                            return Mono.empty();
                        }
                    })
                    .collectList()
                    .block();  // Use block() for a non-reactive application*/
        } catch (Exception e) {
            log.error(">> An exception occurred while making the AWS Lambda request", e);
            // Handle the exception
            return null;
        }

        // 4. 응답 메시지 db에 저장
        String awsMessage = messageRequest.getSenderNickname() + "님의 질문과 유사도가 높은 대화 목록입니다";
        LocalDateTime now = LocalDateTime.now().withNano(0);     // ignore milliseconds
        ChatMessage awsLambdaResponseMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(mentorNickname)
                .message(awsMessage)
                .messageList(awsLambdaResponseList)  // save three of summary_question and answer
                .messageType(MessageType.AI_SUMMARY)
                .time(now)
                .build();

        if (awsLambdaResponseList.size() == 1) {
            String awsListMessage = messageRequest.getSenderNickname() + "님의 질문과 유사도가 높은 대화 목록이 존재하지 않습니다";
            awsLambdaResponseMessage.setLambdaMessage(awsListMessage);
            awsLambdaResponseMessage.setLambdaMessageList(null);
        }

        try {
            messageRepository.save(awsLambdaResponseMessage);
        } catch (RuntimeException e) {
            log.error(">> handleQuestion] save() error occured ", e);
            return null;
        }

        return MessageResponse.fromChatMessageEntity(awsLambdaResponseMessage, null);
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
