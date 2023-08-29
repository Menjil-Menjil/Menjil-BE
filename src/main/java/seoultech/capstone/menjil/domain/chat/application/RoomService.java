package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final MessageService messageService;
    private final AwsS3Handler awsS3Handler;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;    // img_url 정보 조회를 위해, 부득이하게 userRepository 사용
    private final int PAGE_SIZE = 10;
    private final int GET_ROOM_INFO_SIZE = 1;
    private final int AWS_URL_DURATION = 7;
    private final String TYPE_MENTEE = "MENTEE";
    private final String TYPE_MENTOR = "MENTOR";

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 채팅방 입장
     * case 1: 채팅 내역이 존재하지 않는 경우(처음 입장)
     * case 2: 채팅 내역이 존재하는 경우
     */
    public List<MessageResponse> enterTheRoom(RoomDto roomDto) {
        List<MessageResponse> result = new ArrayList<>();

        // case 0: 멘티 혹은 멘토 id가 db에 존재하지 않는 경우 CustomException
        User menteeInDb = userRepository.findUserByNickname(roomDto.getMenteeNickname())
                .orElse(null);
        User mentorInDb = userRepository.findUserByNickname(roomDto.getMentorNickname())
                .orElse(null);
        if (menteeInDb == null) {
            throw new CustomException(ErrorCode.MENTEE_NICKNAME_NOT_EXISTED);
        }
        if (mentorInDb == null) {
            throw new CustomException(ErrorCode.MENTOR_NICKNAME_NOT_EXISTED);
        }

        Room room = roomRepository.findRoomById(roomDto.getRoomId());
        if (room == null) {
            // case 1: 채팅방이 존재하지 않는 경우
            // 먼저 채팅방을 db에 저장한다.
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
            MessageResponse messageResponse = messageService.sendWelcomeMessage(newRoomDto);
            result.add(messageResponse);
        } else {
            // case 2: 채팅방이 존재하는 경우 -> 채팅 메시지가 반드시 존재한다.
            // 최대 10개의 메시지를 클라이언트로 보낸다.
            PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(
                    Sort.Order.desc("time"),
                    Sort.Order.desc("_id") // if time is same, order by _id(because ignore milliseconds in time)
            ));
            List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomDto.getRoomId(), pageRequest);
            for (int i = 0; i < messagePage.size(); i++) {
                MessageResponse dto = MessageResponse.fromChatMessageEntity(messagePage.get(i), messagePage.size() - i);
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 사용자의 채팅방 전체 데이터를 불러오는 경우
     * 멘티는 방 id, 멘토의 닉네임과 img_url, 마지막 대화내용,
     * 멘토는 방 Id, 멘티의 닉네임과 img_url, 마지막 대화내용
     * 리스트 정보가 필요하다.
     */
    public List<RoomInfoResponse> getAllRoomsOfUser(String nickname, String type) {
        List<RoomInfoResponse> result = new ArrayList<>();

        /* type == MENTEE 의 경우(사용자가 멘타인 경우) */
        if (type.equals(TYPE_MENTEE)) {
            List<Room> roomList = roomRepository.findRoomsByMenteeNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }
            for (Room room : roomList) {
                result.add(findMentorRoomsByMentee(room));
            }
        }
        /* type == Mentor 의 경우(사용자가 멘토인 경우) */
        else if (type.equals(TYPE_MENTOR)) {
            List<Room> roomList = roomRepository.findRoomsByMentorNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }
            for (Room room : roomList) {
                result.add(findMenteeRoomsByMentor(room));
            }
        } else {
            throw new CustomException(ErrorCode.TYPE_NOT_ALLOWED);
        }

        result = sortByLastMessagedTimeOfHourDESC(result);
        return result;
    }

    /**
     * 사용자가 '멘티'인 경우
     * '멘토'의 방 정보를 받아온다.
     */
    private RoomInfoResponse findMentorRoomsByMentee(Room room) {
        // Get MENTOR nickname, and Room id
        String mentorNickname = room.getMentorNickname();
        String roomId = room.getId();

        System.out.println("roomId = " + roomId);


        // Get MENTOR img url
        User mentor = userRepository.findUserByNickname(mentorNickname)
                .orElse(null);
        assert mentor != null;  // 멘토가 존재하지 않을 수 없다.

        // 주의! 만료 기간은 최대 7일까지 설정 가능하다.
        String mentorImgUrl = String.valueOf(awsS3Handler.generatePresignedUrl(BUCKET_NAME, mentor.getImgUrl(), Duration.ofDays(AWS_URL_DURATION)));

        // Get Last Message and message time
        PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id") // if time is same, order by _id(because ignore milliseconds in time)
        ));
        List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomId, pageRequest);
        String lastMessage = messagePage.get(0).getMessage();
        LocalDateTime lastMessageTime = messagePage.get(0).getTime();

        return RoomInfoResponse.of(roomId, mentorNickname,
                mentorImgUrl, lastMessage, lastMessageTime);
    }

    /**
     * 사용자가 '멘토'인 경우
     * '멘티'의 방 정보를 받아온다.
     */
    private RoomInfoResponse findMenteeRoomsByMentor(Room room) {
        // Get MENTEE nickname, and Room id
        String menteeNickname = room.getMenteeNickname();
        String roomId = room.getId();

        // Get MENTEE img url
        User mentee = userRepository.findUserByNickname(menteeNickname)
                .orElse(null);
        assert mentee != null;  // 멘티가 존재하지 않을 수 없다.
        // 주의! 만료 기간은 최대 7일까지 설정 가능하다.
        String menteeImgUrl = String.valueOf(awsS3Handler.generatePresignedUrl(BUCKET_NAME, mentee.getImgUrl(), Duration.ofDays(AWS_URL_DURATION)));

        // Get Last Message and message time
        PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id") // if time is same, order by _id(because ignore milliseconds in time)
        ));
        List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomId, pageRequest);
        String lastMessage = messagePage.get(0).getMessage();
        LocalDateTime lastMessageTime = messagePage.get(0).getTime();

        return RoomInfoResponse.of(roomId, menteeNickname,
                menteeImgUrl, lastMessage, lastMessageTime);
    }

    /**
     * Sort by LastMessageTime, order by DESC
     * 클라이언트에서 처리를 원활하게 하기 위해서 정렬하였음.
     */
    private List<RoomInfoResponse> sortByLastMessagedTimeOfHourDESC(List<RoomInfoResponse> result) {
        result = result.stream()
                .sorted(Comparator.comparing(RoomInfoResponse::getLastMessageTime).reversed())
                .collect(Collectors.toList());
        return result;
    }
}
