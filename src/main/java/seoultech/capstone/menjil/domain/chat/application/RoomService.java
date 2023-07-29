package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
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
    private final MongoTemplate mongoTemplate;
    private final int PAGE_SIZE = 10;

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
    public List<MessagesResponse> enterTheRoom(String roomId) {
        List<MessagesResponse> result = new ArrayList<>();

        // db 에서 room 정보가 있는지 먼저 조회한다.
        Room room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new CustomException(ErrorCode.ROOM_NOT_EXISTED);
        }

        // 다음으로 채팅 메시지가 존재하는지 확인한다.
        // List<> findBy ... 메서드로 전체 데이터를 가져올 경우 Overhead 발생. 단순히 exists 로 존재 유무만 확인하면 된다.
        boolean checkMessageInDb = messageRepository.existsChatMessageByRoomId(roomId);

        if (!checkMessageInDb) {
            // case 1: 채팅 내역이 존재하지 않는 경우: Send Welcome Message
            // Room -> RoomDto
            RoomDto roomDto = RoomDto.fromRoom(room);

            // use roomDto in MessageService, and save message.
            MessagesResponse messagesResponse = messageService.sendWelcomeMessage(roomDto);
            result.add(messagesResponse);

        } else {
            // case 2: 채팅 내역이 존재하는 경우: 채팅 내역은 10개씩 클라이언트로 전달한다.
            // 그리고 채팅 내역은 Time 기준 최신 메시지를 보내준다.(혹은 _id 로도 정렬이 가능하다)

            Pageable pageable = PageRequest.of(0, PAGE_SIZE);

            Query query = new Query().with(pageable);
            query.with(Sort.by(Sort.Direction.DESC, "time")); // "_id" 로도 정렬 가능
            query.addCriteria(Criteria.where("room_id").is(roomId));
            List<ChatMessage> messageList = mongoTemplate.find(query, ChatMessage.class);

            for (ChatMessage message : messageList) {
                MessagesResponse dto = MessagesResponse.fromMessage(message);
                result.add(dto);
            }
        }
        return result;
    }

    /* 채팅 메시지를 10개씩 받아오는 메서드 */
    public void getChatMessages() {

    }


}
