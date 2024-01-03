package seoultech.capstone.menjil.domain.chatbot.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chatbot.application.dto.request.ChatBotRoomServiceRequest;
import seoultech.capstone.menjil.domain.chatbot.application.dto.request.DeleteChatBotRoomServiceRequest;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.domain.chatbot.dao.ChatBotRoomRepository;
import seoultech.capstone.menjil.domain.chatbot.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatBotRoomService {

    private final MessageService messageService;
    private final AwsS3Handler awsS3Handler;
    private final ChatBotRoomRepository chatBotRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;    // img_url 정보 조회를 위해, 부득이하게 userRepository 사용

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    public ChatBotRoomIdResponse enterChatBotRoom(ChatBotRoomServiceRequest serviceRequest) {

        // case 0: 사용자 닉네임이 DB에 존재하지 않을 경우 예외발생
        validateUserIsExisted(serviceRequest);

        // case 1: RoomId 생성
        String roomId = createRoomId(serviceRequest.getInitiatorNickname(), serviceRequest.getRecipientNickname());

        ChatBotRoom chatBotRoom = chatBotRoomRepository.findChatBotRoomByRoomId(roomId)
                .orElse(null);
        if (chatBotRoom == null) {
            // case 2: 채팅방이 존재하지 않는 경우
            // 채팅방을 생성해서 웰컴 메시지를 생성한 후, 채팅방 아이디를 전달한다.
            createNewChatBotRoom(roomId, serviceRequest);

            // 여기서 생성 로직을 수행
            return new ChatBotRoomIdResponse(roomId);
        } else {
            // case 2-1: 채팅방이 존재하는 경우
            // 채팅방 id만 전달한다.
            return new ChatBotRoomIdResponse(chatBotRoom.getRoomId());
        }
    }

    private void validateUserIsExisted(ChatBotRoomServiceRequest serviceRequest) {
        Optional<User> initiatorInDb = userRepository.findUserByNickname(serviceRequest.getInitiatorNickname());
        initiatorInDb.orElseThrow(() -> new CustomException(ErrorCode.INITIATOR_USER_NOT_EXISTED));

        Optional<User> receiverInDb = userRepository.findUserByNickname(serviceRequest.getRecipientNickname());
        receiverInDb.orElseThrow(() -> new CustomException(ErrorCode.RECEPIENT_USER_NOT_EXISTED));
    }

    public String createRoomId(String var1, String var2) {
        // 변수들을 결합
        String combined = var1 + "!@#@!@" + var2;

        // 문자열을 바이트 배열로 변환
        byte[] bytes = combined.getBytes();

        // UUID 생성
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    private void createNewChatBotRoom(String roomId, ChatBotRoomServiceRequest serviceRequest) {
        // 1. Save ChatBot Room
        ChatBotRoom newChatBotRoom = saveChatBotRoom(roomId, serviceRequest);

        // 2. Save Chat Message
        boolean createAndSaveWelcomeMsg = messageService.createWelcomeMessage(newChatBotRoom.getRoomId(),
                serviceRequest.getInitiatorNickname(),
                serviceRequest.getRecipientNickname());
        if (!createAndSaveWelcomeMsg) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ChatBotRoom saveChatBotRoom(String roomId, ChatBotRoomServiceRequest serviceRequest) {
        ChatBotRoom newChatBotRoom = ChatBotRoom.builder()
                .roomId(roomId)
                .initiatorNickname(serviceRequest.getInitiatorNickname())
                .recipientNickname(serviceRequest.getRecipientNickname())
                .build();
        try {
            return chatBotRoomRepository.save(newChatBotRoom);
        } catch (RuntimeException e) {
            log.error("ChatBotRoom 저장 시 Runtime 오류 발생!");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 사용자의 챗봇 대화방 목록을 불러온다.
     */
    // TODO: 이 메서드는 추후 개별 함수로 분리 및, 성능 테스트를 시도해볼 것.
    public List<ChatBotRoomResponse> getAllChatBotRooms(String initiatorNickname) {
        List<ChatBotRoom> chatBotRooms = chatBotRoomRepository.findAllByInitiatorNickname(initiatorNickname);

        if (chatBotRooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 챗봇 대화방을 순회하면서, 수신자 닉네임을 가져온다.
        List<String> recipientNicknames = chatBotRooms.stream()
                .map(ChatBotRoom::getRecipientNickname)
                .collect(Collectors.toList());

        List<User> users = userRepository.findAllByNicknameIn(recipientNicknames);

        // User 객체를 recipientNickname을 키로 하는 맵으로 변환
        Map<String, User> nicknameToUser = users.stream()
                .collect(Collectors.toMap(User::getNickname, Function.identity()));

        return chatBotRooms.stream().map(room -> {
            User user = nicknameToUser.get(room.getRecipientNickname());
            if (user == null) {
                // 적절한 예외 처리 또는 대체 로직
                throw new CustomException(ErrorCode.USER_NOT_EXISTED);
            }

            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "time"));
            Page<ChatMessage> chatMessagePage = messageRepository
                    .findByRoomIdAndMessageTypeSortedByTimeDesc(room.getRoomId(), MessageType.C_QUESTION, pageable);

            ChatMessage latestChatMessage = null;
            LocalDateTime latestMessageTime = null;
            if (!chatMessagePage.isEmpty()) {
                latestChatMessage = chatMessagePage.getContent().get(0);
                latestMessageTime = latestChatMessage.getTime();
            }

            return ChatBotRoomResponse.builder()
                    .roomId(room.getRoomId())
                    .recipientNickname(room.getRecipientNickname())
                    .imgUrl(generatePreSignedUrlForUserImage(user))
                    .createdDateTime(room.getCreatedDate())
                    .questionMessage(latestChatMessage != null ? latestChatMessage.getMessage() : null)
                    .questionMessageDateTime(latestMessageTime)
                    .build();
            })
            .sorted(Comparator.comparing(ChatBotRoomResponse::getQuestionMessageDateTime,
                    Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
    }

    private String generatePreSignedUrlForUserImage(User mentor) {
        // 주의! 만료 기간은 최대 7일까지 설정 가능하다.
        int AWS_URL_DURATION = 7;

        return String.valueOf(awsS3Handler.generatePresignedUrl(
                BUCKET_NAME, mentor.getImgUrl(), Duration.ofDays(AWS_URL_DURATION)
        ));
    }

    public boolean quitRoom(DeleteChatBotRoomServiceRequest request) {
        String roomId = request.getRoomId();

        // 1. ChatMessage 데이터 제거
        boolean deleteChatMessagesByRoomId = deleteChatMessagesByRoomId(roomId);

        // 2. Room 제거
        boolean deleteRoom = deleteChatBotRoomByRoomId(roomId);

        return deleteChatMessagesByRoomId && deleteRoom;
    }

    boolean deleteChatMessagesByRoomId(String roomId) {
        try {
            messageRepository.deleteChatMessagesByRoomId(roomId);
            return true;
        } catch (Exception e) {
            log.error("error : ", e);
            return false;
        }
    }

    boolean deleteChatBotRoomByRoomId(String roomId) {
        try {
            chatBotRoomRepository.deleteChatBotRoomByRoomId(roomId);
            return true;
        } catch (Exception e) {
            log.error("error : ", e);
            return false;
        }
    }
}
