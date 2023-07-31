package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    /* 사용자에게 처음 응답 메시지를 보내준다 */
    public MessagesResponse sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String welcomeMessage = "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";

        welcomeMsg.setWelcomeMessage(roomDto.getRoomId(), SenderType.MENTOR,
                roomDto.getMentorNickname(), welcomeMessage, MessageType.ENTER,
                LocalDateTime.now());

        // save entity to mongoDB
        try {
            messageRepository.save(welcomeMsg);
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        // Entity -> Dto
        return MessagesResponse.fromMessage(welcomeMsg, null);
    }

    public boolean saveChatMessage(MessageDto messageDto) {
        // MessageDto -> ChatMessage(Entity) 변환
        ChatMessage chatMessage = MessageDto.fromMessageDto(messageDto);

        // save entity to mongoDB
        // 저장이 잘된 경우 true, 그렇지 않은 경우 false 리턴
        try {
            messageRepository.save(chatMessage);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }
}
