package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    /* 사용자에게 처음 응답 메시지를 보내준다 */
    public MessageDto sendWelcomeMessage(RoomDto roomDto) {
        ChatMessage welcomeMsg = new ChatMessage();
        String welcomeMessage = "안녕하세요 " + roomDto.getMenteeNickname() + "님!\n"
                + "멘토 " + roomDto.getMentorNickname() + "입니다. 질문을 입력해주세요";

        welcomeMsg.setWelcomeMessage(roomDto.getRoomId(),
                roomDto.getMentorNickname(), welcomeMessage, MessageType.ENTER,
                LocalDateTime.now());

        // save Entity
        messageRepository.save(welcomeMsg);

        // Entity -> Dto
        MessageDto messageDto = MessageDto.fromMessage(welcomeMsg);

        return messageDto;
    }

    public boolean saveChatMessage(MessageDto messageDto) {
        // MessageDto -> Message(Entity) 변환

        // MongoDB 에 저장


        // 저장이 잘된 경우 true, 그렇지 않은 경우 false 리턴


        return true;
    }
}
