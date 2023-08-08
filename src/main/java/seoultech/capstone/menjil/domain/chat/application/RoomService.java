package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomListResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final MessageService messageService;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final String SPECIAL_CHARACTER1 = "##*&^";
    private final String SPECIAL_CHARACTER2 = "^_$#@";
    private final int PAGE_SIZE = 10;
    private final int GET_ROOM_INFO = 1;
    private final String TYPE_MENTEE = "MENTEE";
    private final String TYPE_MENTOR = "MENTOR";

    /**
     * 채팅방 입장
     * case 1: 채팅 내역이 존재하지 않는 경우(처음 입장)
     * case 2: 채팅 내역이 존재하는 경우
     */
    public List<MessagesResponse> enterTheRoom(RoomDto roomDto) {
        List<MessagesResponse> result = new ArrayList<>();

        Room room = roomRepository.findRoomById(roomDto.getRoomId());
        if (room == null) {
            // case 1: 채팅방이 존재하지 않는 경우
            // 먼저 채팅방을 저장한다.
            Room newRoom = Room.builder()
                    .roomId(roomDto.getRoomId())
                    .menteeNickname(roomDto.getMenteeNickname())
                    .mentorNickname(roomDto.getMentorNickname())
                    .build();
            try {
                roomRepository.save(newRoom);
            } catch (RuntimeException e) {
                throw new CustomException(ErrorCode.SERVER_ERROR);
            }

            RoomDto newRoomDto = RoomDto.fromRoom(newRoom);
            MessagesResponse messagesResponse = messageService.sendWelcomeMessage(newRoomDto);
            result.add(messagesResponse);
        } else {
            // case 2: 채팅방이 존재하는 경우 -> 채팅 메시지가 반드시 존재한다.
            List<ChatMessage> messageList = getOrderedChatMessagesByRoomId(PAGE_SIZE, roomDto.getRoomId());
            for (int i = 0; i < messageList.size(); i++) {
                MessagesResponse dto = MessagesResponse.fromMessage(messageList.get(i), messageList.size() - i);
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 사용자의 채팅방 전체 데이터를 불러오는 경우
     * 멘티는 멘토의 닉네임과 마지막 대화내용,
     * 멘토는 멘티의 닉네임과 마지막 대화내용 리스트 정보가 필요하다.
     */
    public List<RoomListResponse> getAllRooms(String nickname, String type) {
        List<RoomListResponse> result = new ArrayList<>();

        if (type.equals(TYPE_MENTOR)) {
            List<Room> roomList = roomRepository.findRoomsByMentorNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }

            for (Room room : roomList) {
                // get mentee nickname and last message in room
                String menteeNickname = room.getMenteeNickname();
                String roomId = room.getId();

                List<ChatMessage> messageList = getOrderedChatMessagesByRoomId(GET_ROOM_INFO, roomId);
                String lastMessage = messageList.get(0).getMessage();

                result.add(RoomListResponse.builder()
                        .lastMessage(lastMessage)
                        .nickname(menteeNickname)
                        .build());
            }

        } else if (type.equals(TYPE_MENTEE)) {
            List<Room> roomList = roomRepository.findRoomsByMenteeNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }

            for (Room room : roomList) {
                // get mentor nickname and last message in room
                String mentorNickname = room.getMentorNickname();
                String roomId = room.getId();

                List<ChatMessage> messageList = getOrderedChatMessagesByRoomId(GET_ROOM_INFO, roomId);
                System.out.println("messageList.size() = " + messageList.size());
                String lastMessage = messageList.get(0).getMessage();

                result.add(RoomListResponse.builder()
                        .lastMessage(lastMessage)
                        .nickname(mentorNickname)
                        .build());
            }
        } else {
            throw new CustomException(ErrorCode.TYPE_NOT_ALLOWED);
        }
        return result;
    }

    private List<ChatMessage> getOrderedChatMessagesByRoomId(int size, String roomId) {
        Pageable pageable = PageRequest.of(0, size);
        Sort sort = Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id") // Ignore milliseconds, so order _id if time is same
        );
        Query query = new Query().with(pageable).with(sort);
        query.addCriteria(Criteria.where("room_id").is(roomId));
        return mongoTemplate.find(query, ChatMessage.class);
    }

}
