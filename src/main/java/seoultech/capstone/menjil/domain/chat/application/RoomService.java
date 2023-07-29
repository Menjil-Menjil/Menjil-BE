package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final int PAGE = 10;

    /* 채팅방 생성 */
    public int createRoom(RoomDto roomDto) {
        // Dto -> Entity
        Room room = roomDto.toRoom();

        // save db
        try {
            // room.setRoomId(roomDto.getRoomId()); // Assign a value to the ID field manually
            roomRepository.save(room);
        } catch (DataIntegrityViolationException de) {
            // room Entity의 room_id 가 중복되는 경우
            // UUID를 사용하기 때문에 중복될 가능성이 매우 낮지만, 혹시 모르니 예외처리
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return HttpStatus.CREATED.value();
    }

    /* 채팅방 입장(생성한 뒤) */
    public List<MessageDto> enterTheRoom(String roomId) {
        List<MessageDto> result = new ArrayList<>();

        // db 접근해서 mentor_id, mentor_nickname (sender 정보)를 가져온다.
        Room room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new CustomException(ErrorCode.ROOM_NOT_EXISTED);
        }

        // 다음으로 채팅 메시지가 존재하는지 확인해야 한다.
        List<ChatMessage> messageList = messageRepository.findChatMessagesByRoomId(roomId);

        if (messageList.isEmpty()) {
            // 1. 채팅 내역이 존재하지 않는 경우
            // Room -> RoomDto
            RoomDto roomDto = RoomDto.fromRoom(room);

            // use roomDto in MessageService, and save message.
            MessageDto messageDto = messageService.sendWelcomeMessage(roomDto);
            result.add(messageDto);

            return result;
        } else {
            // 2. 기존에 채팅 내역이 존재하는 경우: 채팅 내역은 10개씩 보여준다.
            // 그리고 채팅 내역은 Time 기준 최신 순으로 보내준다.
            // 2-1. 채팅 내역이 10개 미만인 경우
            if (messageList.size() < PAGE) {

            }


            // 2-2. 채팅 내역이 10개 이상인 경우

            return null;
        }

    }
}
