package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.MessageDto;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.servlet.http.HttpSession;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final MessageService messageService;
    private final HttpSession httpSession;

    /* 채팅방 생성 */
    public int createRoom(RoomDto roomDto) {
        // Dto -> Entity
        Room room = roomDto.toRoom();

        // save db
        try {
            room.setRoomId(roomDto.getRoomId()); // Assign a value to the ID field manually
            roomRepository.save(room);
        } catch (DataIntegrityViolationException de) {
            // room Entity의 room_id 가 중복되는 경우
            // UUID를 사용하기 때문에 중복될 가능성이 매우 낮지만, 혹시 모르니 예외처리
            log.error("DataIntegrityViolationException err : ", de);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        } catch (Exception e) {
            log.error("Exception err : ", e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return HttpStatus.CREATED.value();
    }

    /* 채팅방 입장(생성한 뒤) */
    public MessageDto enterTheRoom(String roomId) {

        // db 접근해서 mentor_id, mentor_nickname (sender 정보)를 가져온다.
        Room room;
        try {
            room = roomRepository.findRoomByRoomId(roomId);
        } catch (NullPointerException e) {
            throw new CustomException(ErrorCode.ROOM_NOT_EXISTED);
        }

        // Room -> RoomDto
        RoomDto roomDto = RoomDto.fromRoom(room);

        // use roomDto in MessageService, and save message.
        MessageDto messageDto = messageService.sendWelcomeMessage(roomDto);

        return messageDto;
    }
}
