package seoultech.capstone.menjil.domain.chat.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.request.FlaskRequestDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageService {

    private final WebClient flaskWebClient;
    private final ChatGptService chatGptService;
    private final MessageRepository messageRepository;

    public MessageService(@Qualifier("chatGptWebClient") WebClient flaskWebClient,
                          ChatGptService chatGptService, MessageRepository messageRepository) {
        this.flaskWebClient = flaskWebClient;
        this.chatGptService = chatGptService;
        this.messageRepository = messageRepository;
    }

    /**
     * 사용자에게 처음 응답 메시지를 보내준다.
     */
    public MessagesResponse sendWelcomeMessage(RoomDto roomDto, String roomId) {
        ChatMessage welcomeMsg = new ChatMessage();
        String welcomeMessage = "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";

        welcomeMsg.setWelcomeMessage(roomId, SenderType.MENTOR,
                roomDto.getMentorNickname(), welcomeMessage, MessageType.ENTER,
                LocalDateTime.now());

        // save entity to mongoDB
        try {
            messageRepository.save(welcomeMsg);
        } catch (RuntimeException e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        // Entity -> Dto
        return MessagesResponse.fromMessage(welcomeMsg, null);
    }

    /**
     * 채팅 메시지를 저장한다.
     */
    public boolean saveChatMessage(MessageDto messageDto) {
        // MessageDto -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = MessageDto.fromMessageDto(messageDto);

        // save entity to mongoDB
        // 저장이 잘된 경우 true, 그렇지 않은 경우 false 리턴
        try {
            messageRepository.save(chatMessage);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    /**
     * MessageType: QUESTION 인 경우
     */
    public String handleQuestion(String roomId, MessageDto messageDto) {

        // 1. 채팅 메시지 저장
        boolean saveMsg = saveChatMessage(messageDto);
        if (!saveMsg) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        // 2. ChatGPT에게 질문 데이터 전달하여 세줄 요약 결과를 받아온다.
        Message message = chatGptService.getMessageFromGpt(messageDto.getMessage());

        // 3. 원본 메시지, 세 줄 요약 메시지, 멘티 닉네임, 멘토 닉네임을 Flask 서버로 전달한 뒤, 결과를 받아온다.
        FlaskRequestDto flaskRequestDto = FlaskRequestDto.builder()
                .mentorNickname(findMentorNickname(roomId))     // find Mentor nickname
                .menteeNickname(messageDto.getSenderNickname())
                .originMessage(messageDto.getMessage())
                .threeLineSummaryMessage(message.getContent())
                .build();

        // Make the POST request and block to get the response
        String response = flaskWebClient.post()
                .uri("/your-endpoint")
                .body(BodyInserters.fromValue(flaskRequestDto))
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Use block() for a non-reactive application*/

        return response;
    }

    /**
     * 멘토의 닉네임을 가져오는 메서드
     * 방 id로 채팅 내역을 조회한 다음, messageType: ENTER 인 값을 찾는다.
     */
    public String findMentorNickname(String roomId) {
        ChatMessage message = messageRepository.findChatMessageByRoomIdAndMessageType(roomId, MessageType.ENTER);
        return message.getSenderNickname();
    }
}
