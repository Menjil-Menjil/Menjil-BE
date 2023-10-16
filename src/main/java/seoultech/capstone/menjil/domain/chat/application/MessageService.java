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
import seoultech.capstone.menjil.domain.chat.dto.response.MessageOrderResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static seoultech.capstone.menjil.global.exception.ErrorIntValue.INTERNAL_SERVER_ERROR;
import static seoultech.capstone.menjil.global.exception.ErrorIntValue.TIME_INPUT_INVALID;

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

    public Optional<MessageOrderResponse> sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = createWelcomeMessage(roomDto);
        if (saveChatMessageInDb(welcomeMsg)) {
            return Optional.of(convertChatMessageToDto(welcomeMsg));
        }
        return Optional.empty();
    }

    public Object saveAndSendClientChatMessage(MessageRequest messageRequest) {
        // MessageRequest의 time format 검증
        Optional<LocalDateTime> dateTimeOptional = parseDateTime(messageRequest.getTime());
        if (dateTimeOptional.isEmpty()) {
            return TIME_INPUT_INVALID.getValue(); // or handle the error differently
        }
        LocalDateTime dateTime = dateTimeOptional.get();

        // MessageRequest -> ChatMessage(Entity) 변환
        ChatMessage clientChatMessage = convertMessageRequestToChatMessageEntity(messageRequest, dateTime);

        // save entity to mongoDB
        if (!saveChatMessageInDb(clientChatMessage)) {
            return INTERNAL_SERVER_ERROR.getValue();  // handle the failure case appropriately
        }

        return MessageResponse.fromChatMessageEntity(clientChatMessage);
    }

    // TODO: 추후 사용되지 않으면 지울 것
   /* public int saveChatMessage(MessageRequest messageRequest) {
        // MessageRequest time format 검증
        Optional<LocalDateTime> dateTimeOptional = parseDateTime(messageRequest.getTime());

        if (dateTimeOptional.isEmpty()) {
            return TIME_INPUT_INVALID.getValue(); // or handle the error differently
        }
        LocalDateTime dateTime = dateTimeOptional.get();

        // MessageRequest -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = convertMessageRequestToChatMessageEntity(messageRequest, dateTime);
        System.out.println("chatMessage.get_id() = " + chatMessage.get_id());

        System.out.println("========");

        // save entity to mongoDB
        if (!saveChatMessageInDb(chatMessage)) {
            return INTERNAL_SERVER_ERROR.getValue();  // handle the failure case appropriately
        }
        System.out.println("chatMessage.get_id() = " + chatMessage.get_id());
        return SUCCESS.getValue();
    }

    public MessageResponse sendClientMessage(MessageRequest messageRequest) {
        Optional<LocalDateTime> dateTimeOptional = parseDateTime(messageRequest.getTime());

        if (dateTimeOptional.isEmpty()) {
            // datetime parse exception
            return null;
        }
        LocalDateTime dateTime = dateTimeOptional.get();

        return MessageResponse.builder()
                .roomId(messageRequest.getRoomId())
                .senderType(messageRequest.getSenderType())
                .senderNickname(messageRequest.getSenderNickname())
                .message(messageRequest.getMessage())
                .messageList(null)
                .messageType(messageRequest.getMessageType())
                .time(dateTime)
                .build();
    }*/

    public MessageResponse sendAIMessage(String roomId, MessageRequest messageRequest) {
        String specificMessage = "당신의 궁금증을 빠르게 해결할 수 있게 도와드릴게요!";

        LocalDateTime now = getCurrentTimeWithNanos();

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
            return null;  // DB save error: server error
        }
        return MessageResponse.fromChatMessageEntity(message);
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

        return MessageResponse.fromChatMessageEntity(lambdaResponseChatMessage);
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
     * Used By sendWelcomeMessage
     */
    private ChatMessage createWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String roomId = roomDto.getRoomId();
        SenderType type = SenderType.MENTOR;
        String mentorNickname = roomDto.getMentorNickname();
        String welcomeMessage = buildWelcomeMessageText(roomDto);
        Object messageList = null;
        MessageType messageType = MessageType.ENTER;
        LocalDateTime now = getCurrentTimeWithNanos();

        welcomeMsg.setWelcomeMessage(roomId, type, mentorNickname, welcomeMessage, messageList, messageType, now);
        return welcomeMsg;
    }

    private String buildWelcomeMessageText(RoomDto roomDto) {
        return "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";
    }

    /**
     * Used by saveChatMessage
     */
    private ChatMessage convertMessageRequestToChatMessageEntity(MessageRequest messageRequest, LocalDateTime dateTime) {
        return MessageRequest.toChatMessageEntity(messageRequest, dateTime);
    }

    /**
     * Used By handleQuestion
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
        String awsMessage = "먼저, " + messageRequest.getSenderNickname()
                + "님의 질문과 유사한 질문에서 시작된 대화를 살펴 보실래요?\n"
                + "더 신속하게, 다양한 해답을 얻을 수 있을거에요";

        String message4th = "AI 챗봇을 종료하고 멘토 답변 기다리기";

        LocalDateTime now = getCurrentTimeWithNanos();

        // add 4th response
        awsLambdaResponses.add(AwsLambdaResponse.of(null,
                message4th, null, null));

        ChatMessage awsLambdaResponseMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderType(SenderType.MENTOR)
                .senderNickname(mentorNickname)
                .message(awsMessage)
                .messageList(awsLambdaResponses)  // save three of summary question and answer
                .messageType(MessageType.AI_SUMMARY_LIST)
                .time(now)
                .build();

        // TODO: 추후 디자인에 따라 이 부분 변동 가능성 있음.
        if (similarMessageDoesNotExists(awsLambdaResponses)) {
            /*String similarDoesNotExists = messageRequest.getSenderNickname()
                    + "님의 질문과 유사도가 높은 대화 목록이 존재하지 않습니다";
            awsLambdaResponseMessage.setLambdaMessage(similarDoesNotExists);*/
            awsLambdaResponseMessage.setLambdaMessageList(AwsLambdaResponse.of(null,
                    messageRequest.getSenderNickname()
                            + "님의 질문과 유사도가 높은 대화 목록이 존재하지 않습니다", null, null)
            );
        }
        return awsLambdaResponseMessage;
    }

    public boolean similarMessageDoesNotExists(List<AwsLambdaResponse> awsLambdaResponses) {
        return awsLambdaResponses.size() == 1;
    }

    /**
     * Used in Common
     */
    private MessageOrderResponse convertChatMessageToDto(ChatMessage message) {
        return MessageOrderResponse.fromChatMessageEntity(message, null);
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

    private boolean saveChatMessageInDb(ChatMessage message) {
        try {
            messageRepository.save(message);
            return true;
        } catch (RuntimeException e) {
            log.error(">> messageRepository.save() error occured ", e);
            return false;
        }
    }

    private LocalDateTime getCurrentTimeWithNanos() {
        return LocalDateTime.now(); // not ignore milliseconds
    }
}
