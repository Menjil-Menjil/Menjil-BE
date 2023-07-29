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

        // case 1: Mentee <-> Mentor 간에 이미 채팅방이 생성되어 있는 경우, 중복으로 생성될 수 없다.
        Room existsRoomInDb = roomRepository.findRoomByMenteeNicknameAndMentorNickname(
                roomDto.getMenteeNickname(), roomDto.getMentorNickname());
        if (existsRoomInDb != null) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        // save db
        try {
            roomRepository.save(room);
        } catch (DataIntegrityViolationException de) {
            // case 2: UUID 로 생성된 roomId 가 중복된 값인 경우
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        } catch (Exception e) {
            // case 3: 그 외
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
