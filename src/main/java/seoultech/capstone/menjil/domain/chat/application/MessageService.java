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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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

    public Optional<MessageResponse> sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = createWelcomeMessage(roomDto);
        if (saveChatMessageInDb(welcomeMsg)) {
            return Optional.of(convertChatMessageToDto(welcomeMsg));
        }
        return Optional.empty();
    }

    public int saveChatMessage(MessageRequest messageRequest) {
        int SAVE_SUCCESS = 0;
        int TIME_INPUT_INVALID = -1;
        int INTERNAL_SERVER_ERROR = -100;

        // MessageRequest time format 검증
        Optional<LocalDateTime> dateTimeOptional = parseDateTime(messageRequest.getTime());

        if (dateTimeOptional.isEmpty()) {
            return TIME_INPUT_INVALID; // or handle the error differently
        }
        LocalDateTime dateTime = dateTimeOptional.get();

        // MessageRequest -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = convertMessageRequestToChatMessageEntity(messageRequest, dateTime);

        // save entity to mongoDB
        if (!saveChatMessageInDb(chatMessage)) {
            return INTERNAL_SERVER_ERROR;  // handle the failure case appropriately
        }
        return SAVE_SUCCESS;
    }

    /**
     * MessageType: QUESTION
     */
    public MessageResponse sendClientMessage(MessageRequest messageRequest) {
        Optional<LocalDateTime> dateTimeOptional = parseDateTime(messageRequest.getTime());

        if (dateTimeOptional.isEmpty()) {
            return null; // or handle the error differently
        }
        LocalDateTime dateTime = dateTimeOptional.get();

        return MessageResponse.builder()
                .order(null)
                .roomId(messageRequest.getRoomId())
                .senderType(messageRequest.getSenderType())
                .senderNickname(messageRequest.getSenderNickname())
                .message(messageRequest.getMessage())
                .messageList(null)
                .messageType(messageRequest.getMessageType())
                .time(dateTime)
                .build();
    }

    public MessageResponse sendAIMessage(String roomId, MessageRequest messageRequest) {
        String specificMessage = "당신의 궁금증을 빠르게 해결할 수 있게 도와줄 AI 서포터입니다.\n" +
                "멘토의 답변을 기다리면서, 당신의 질문과 유사한 질문에서 시작된 대화를 살펴보실래요?\n" +
                "더 신속하게, 다양한 해답을 얻을 수도 있을 거예요!";

        LocalDateTime now = getCurrentTimeWithoutNanos();

        // Build AI Message from messageRequest
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(findMentorNickname(roomId, messageRequest.getSenderNickname()))
                .message(specificMessage)
                .messageType(MessageType.AI_QUESTION_RESPONSE)
                .time(now)
                .build();

        if (!saveChatMessageInDb(message)) {
            return null;  // handle the failure case appropriately
        }
        return MessageResponse.fromChatMessageEntity(message, null);
    }

    public MessageResponse handleQuestion(String roomId, MessageRequest messageRequest) {
        String mentorNickname = findMentorNickname(roomId, messageRequest.getSenderNickname());

        // 1. ChatGPT에게 질문 데이터 전달하여 세줄 요약 결과를 받아온다.
        Message message = fetchGptMessage(messageRequest.getMessage());

        List<AwsLambdaResponse> awsLambdaResponses = fetchLambdaResponses(messageRequest, mentorNickname, message);
        if (awsLambdaResponses == null) {
            return null; // Handle appropriately
        }

        // 4. create Lambda ChatMessage Entity
        ChatMessage lambdaResponseChatMessage = createLambdaChatMessage(roomId, mentorNickname,
                messageRequest, awsLambdaResponses);

        // 5. 응답 메시지 db에 저장
        if (!saveChatMessageInDb(lambdaResponseChatMessage)) {
            return null;  // handle the failure case appropriately
        }
        return MessageResponse.fromChatMessageEntity(lambdaResponseChatMessage, null);
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

    /**
     * Used by sendWelcomeMessage
     */
    private ChatMessage createWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String roomId = roomDto.getRoomId();
        SenderType type = SenderType.MENTOR;
        String mentorNickname = roomDto.getMentorNickname();
        String welcomeMessage = buildWelcomeMessageText(roomDto);
        Object messageList = null;
        MessageType messageType = MessageType.ENTER;
        LocalDateTime now = getCurrentTimeWithoutNanos();

        welcomeMsg.setWelcomeMessage(roomId, type, mentorNickname, welcomeMessage, messageList, messageType, now);
        return welcomeMsg;
    }

    private String buildWelcomeMessageText(RoomDto roomDto) {
        return "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";
    }

    private MessageResponse convertChatMessageToDto(ChatMessage message) {
        return MessageResponse.fromChatMessageEntity(message, null);
    }

    /**
     * Used by saveChatMessage
     */
    private ChatMessage convertMessageRequestToChatMessageEntity(MessageRequest messageRequest, LocalDateTime dateTime) {
        return MessageRequest.toChatMessageEntity(messageRequest, dateTime);
    }

    /**
     * Used by handleQuestion
     */
    private Message fetchGptMessage(String userMessage) {
        return chatGptService.getMessageFromGpt(userMessage);
    }

    private List<AwsLambdaResponse> fetchLambdaResponses(MessageRequest messageRequest, String mentorNickname, Message message) {
        // 2. Create Request
        AwsLambdaRequest awsLambdaRequest = AwsLambdaRequest.of(
                messageRequest.getSenderNickname(),
                mentorNickname,
                messageRequest.getMessage(),
                message.getContent()
        );

        // 3. Send Request
        try {
            return apiGatewayClient.post()
                    .uri("/api/lambda/question")
                    .body(BodyInserters.fromValue(awsLambdaRequest))
                    .retrieve()
                    .bodyToFlux(AwsLambdaResponse.class)
                    .timeout(Duration.ofSeconds(180))
                    .onErrorResume(e -> {
                        log.error("An error occurred while fetching from Lambda", e);
                        return Mono.empty();
                    })
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error(">> An exception occurred while making the AWS Lambda request", e);
            return null;
        }
    }

    private ChatMessage createLambdaChatMessage(String roomId,
                                                String mentorNickname,
                                                MessageRequest messageRequest,
                                                List<AwsLambdaResponse> awsLambdaResponses) {
        String awsMessage = messageRequest.getSenderNickname() + "님의 질문과 유사도가 높은 대화 목록입니다";
        LocalDateTime now = getCurrentTimeWithoutNanos();

        // add 4th response
        awsLambdaResponses.add(AwsLambdaResponse.of(
                "4. 멘토에게 질문하고 답변을 기다릴래요", null, null));

        ChatMessage awsLambdaResponseMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(mentorNickname)
                .message(awsMessage)
                .messageList(awsLambdaResponses)  // save three of summary_question and answer
                .messageType(MessageType.AI_SUMMARY)
                .time(now)
                .build();

        if (similarMessageDoesNotExists(awsLambdaResponses)) {
            String similarDoesNotExists = messageRequest.getSenderNickname()
                    + "님의 질문과 유사도가 높은 대화 목록이 존재하지 않습니다";
            awsLambdaResponseMessage.setLambdaMessage(similarDoesNotExists);
            awsLambdaResponseMessage.setLambdaMessageList(null);
        }
        return awsLambdaResponseMessage;
    }

    public boolean similarMessageDoesNotExists(List<AwsLambdaResponse> awsLambdaResponses) {
        return awsLambdaResponses.size() == 1;
    }

    /**
     * Used in Common
     */
    private boolean saveChatMessageInDb(ChatMessage message) {
        try {
            messageRepository.save(message);
            return true;
        } catch (RuntimeException e) {
            log.error(">> messageRepository.save() error occured ", e);
            return false;
        }
    }

    public Optional<LocalDateTime> parseDateTime(String timeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return Optional.of(LocalDateTime.parse(timeString, formatter));
        } catch (DateTimeParseException e) {
            log.error(">> Failed to parse date-time string.", e);
            return Optional.empty();
        }
    }

    private LocalDateTime getCurrentTimeWithoutNanos() {
        return LocalDateTime.now().withNano(0); // ignore milliseconds
    }
}
