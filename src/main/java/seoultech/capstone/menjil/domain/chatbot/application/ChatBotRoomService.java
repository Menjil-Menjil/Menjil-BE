package seoultech.capstone.menjil.domain.chatbot.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.ChatBotRoomDto;
import seoultech.capstone.menjil.domain.chatbot.api.dto.request.DeleteChatBotRoomRequest;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomIdResponse;
import seoultech.capstone.menjil.domain.chatbot.application.dto.response.ChatBotRoomResponse;
import seoultech.capstone.menjil.domain.chatbot.dao.ChatBotRoomRepository;
import seoultech.capstone.menjil.domain.chatbot.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.util.*;

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

    public ChatBotRoomIdResponse enterChatBotRoom(ChatBotRoomDto chatBotRoomDto) {

        // case 0: 사용자 닉네임이 DB에 존재하지 않을 경우 예외발생
        validateUserIsExisted(chatBotRoomDto);

        // case 1: RoomId 생성
        String roomId = createRoomId(chatBotRoomDto.getInitiatorNickname(), chatBotRoomDto.getRecipientNickname());

        ChatBotRoom chatBotRoom = chatBotRoomRepository.findChatBotRoomByRoomId(roomId)
                .orElse(null);
        if (chatBotRoom == null) {
            // case 2: 채팅방이 존재하지 않는 경우
            // 채팅방을 생성해서 웰컴 메시지를 생성한 후, 채팅방 아이디를 전달한다.
            createNewChatBotRoom(roomId, chatBotRoomDto);

            // 여기서 생성 로직을 수행
            return new ChatBotRoomIdResponse(roomId);
        } else {
            // case 2-1: 채팅방이 존재하는 경우
            // 채팅방 id만 전달한다.
            return new ChatBotRoomIdResponse(chatBotRoom.getRoomId());
        }
    }

    private void validateUserIsExisted(ChatBotRoomDto chatBotRoomDto) {
        Optional<User> initiatorInDb = userRepository.findUserByNickname(chatBotRoomDto.getInitiatorNickname());
        initiatorInDb.orElseThrow(() -> new CustomException(ErrorCode.INITIATOR_USER_NOT_EXISTED));

        Optional<User> receiverInDb = userRepository.findUserByNickname(chatBotRoomDto.getRecipientNickname());
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

    private void createNewChatBotRoom(String roomId, ChatBotRoomDto chatBotRoomDto) {
        // 1. Save ChatBot Room
        ChatBotRoom newChatBotRoom = saveChatBotRoom(roomId, chatBotRoomDto);

        // 2. Save Chat Message
        boolean createAndSaveWelcomeMsg = messageService.createWelcomeMessage(newChatBotRoom.getRoomId(),
                chatBotRoomDto.getInitiatorNickname(),
                chatBotRoomDto.getRecipientNickname());
        if (!createAndSaveWelcomeMsg) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ChatBotRoom saveChatBotRoom(String roomId, ChatBotRoomDto chatBotRoomDto) {
        ChatBotRoom newChatBotRoom = ChatBotRoom.builder()
                .roomId(roomId)
                .initiatorNickname(chatBotRoomDto.getInitiatorNickname())
                .recipientNickname(chatBotRoomDto.getRecipientNickname())
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
    public List<ChatBotRoomResponse> getAllChatBotRooms(String initiatorNickname) {
        List<ChatBotRoom> chatBotRooms = chatBotRoomRepository.findAllByInitiatorNickname(initiatorNickname);

        if (chatBotRooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 챗봇 대화방을 순회하면서, 수신자 닉네임을 가져온다.
        List<String> recipientNicknames = new ArrayList<>();
        for (ChatBotRoom c : chatBotRooms) {
            recipientNicknames.add(c.getRecipientNickname());
        }

        List<String> recipientImgUrls = new ArrayList<>();
        for (String nickname: recipientNicknames) {
            User user = userRepository.findUserByNickname(nickname).orElse(null);
            assert user != null;
            recipientImgUrls.add(generatePreSignedUrlForUserImage(user));
        }

        List<ChatBotRoomResponse> result = new ArrayList<>();
        for (int i = 0; i < chatBotRooms.size(); i++) {
            ChatBotRoomResponse response = ChatBotRoomResponse.of(
                    chatBotRooms.get(i).getRoomId(),  chatBotRooms.get(i).getRecipientNickname(),
                    recipientImgUrls.get(i));
            result.add(response);
        }
        return result;
    }

    private String generatePreSignedUrlForUserImage(User mentor) {
        // 주의! 만료 기간은 최대 7일까지 설정 가능하다.
        int AWS_URL_DURATION = 7;

        return String.valueOf(awsS3Handler.generatePresignedUrl(
                BUCKET_NAME, mentor.getImgUrl(), Duration.ofDays(AWS_URL_DURATION)
        ));
    }

    public boolean quitRoom(DeleteChatBotRoomRequest request) {
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
