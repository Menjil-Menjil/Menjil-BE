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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final MessageService messageService;
    private final AwsS3Handler awsS3Handler;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;    // img_url 정보 조회를 위해, 부득이하게 userRepository 사용

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    public List<MessageResponse> enterTheRoom(RoomDto roomDto) {

        // case 0: 멘티 혹은 멘토 id가 db에 존재하지 않는 경우 CustomException 발생
        validateUserIsExist(roomDto);

        Room room = roomRepository.findRoomById(roomDto.getRoomId());
        if (room == null) {
            // case 1: 채팅방이 존재하지 않는 경우
            return handleNewRoom(roomDto);
        } else {
            // case 2: 채팅방이 존재하는 경우 -> 채팅 메시지가 반드시 존재한다.
            return handleExistingRoom(roomDto);
        }
    }

    /**
     * 사용자의 채팅방 전체 데이터를 불러오는 경우
     * 멘티는 방 id, 멘토의 닉네임과 img_url, 마지막 대화내용,
     * 멘토는 방 Id, 멘티의 닉네임과 img_url, 마지막 대화내용
     */
    public List<RoomInfoResponse> getAllRoomsOfUser(String nickname, String type) {
        String TYPE_MENTEE = "MENTEE";
        String TYPE_MENTOR = "MENTOR";

        /* type == MENTEE 의 경우(사용자가 멘티인 경우) */
        if (type.equals(TYPE_MENTEE)) {
            return getMenteeRooms(nickname);
        }
        /* type == Mentor 의 경우(사용자가 멘토인 경우) */
        else if (type.equals(TYPE_MENTOR)) {
            return getMentorRooms(nickname);
        } else {
            throw new CustomException(ErrorCode.TYPE_NOT_ALLOWED);
        }
    }

    /**
     * Used By enterTheRoom
     */
    protected void validateUserIsExist(RoomDto roomDto) {
        Optional<User> menteeInDb = userRepository.findUserByNickname(roomDto.getMenteeNickname());
        Optional<User> mentorInDb = userRepository.findUserByNickname(roomDto.getMentorNickname());

        menteeInDb.orElseThrow(() -> new CustomException(ErrorCode.MENTEE_NICKNAME_NOT_EXISTED));
        mentorInDb.orElseThrow(() -> new CustomException(ErrorCode.MENTOR_NICKNAME_NOT_EXISTED));
    }

    protected List<MessageResponse> handleNewRoom(RoomDto roomDto) {
        List<MessageResponse> result = new ArrayList<>();

        Room newRoom = saveNewRoom(roomDto);

        MessageResponse messageResponse = messageService.sendWelcomeMessage(RoomDto.fromRoom(newRoom));
        result.add(messageResponse);

        return result;
    }

    private Room saveNewRoom(RoomDto roomDto) {
        Room newRoom = Room.builder()
                .roomId(roomDto.getRoomId())
                .menteeNickname(roomDto.getMenteeNickname())
                .mentorNickname(roomDto.getMentorNickname())
                .build();

        try {
            return roomRepository.save(newRoom);
        } catch (RuntimeException e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    private List<MessageResponse> handleExistingRoom(RoomDto roomDto) {
        int PAGE_SIZE = 10;

        // 최대 10개의 메시지를 클라이언트로 보낸다.
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id")
        ));

        List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomDto.getRoomId(), pageRequest);

        // index가 낮을 수록, 시간 순이 나중인 메시지를 담도록
        return IntStream.range(0, messagePage.size())
                .mapToObj(i -> MessageResponse.fromChatMessageEntity(messagePage.get(i), messagePage.size() - i))
                .collect(Collectors.toList());
    }

    /**
     * Used By getAllRoomsOfUser
     */
    private List<RoomInfoResponse> getMenteeRooms(String menteeNickname) {
        List<Room> menteeRooms = roomRepository.findRoomsByMenteeNickname(menteeNickname);
        return getRoomsInfo(menteeRooms, this::findMentorRoomsByMentee);
    }

    private List<RoomInfoResponse> getMentorRooms(String mentorNickname) {
        List<Room> roomList = roomRepository.findRoomsByMentorNickname(mentorNickname);
        return getRoomsInfo(roomList, this::findMenteeRoomsByMentor);
    }

    private List<RoomInfoResponse> getRoomsInfo(List<Room> rooms, Function<Room, RoomInfoResponse> mapper) {
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoomInfoResponse> result = rooms.stream()
                .map(mapper)
                .collect(Collectors.toList());
        return sortByLastMessagedTimeOfHourASC(result);
    }

    /**
     * 사용자가 '멘티'인 경우
     * '멘토'의 방 정보를 받아온다.
     */
    private RoomInfoResponse findMentorRoomsByMentee(Room room) {
        // Get MENTOR nickname, and Room id
        String mentorNickname = room.getMentorNickname();
        String roomId = room.getId();

        // Find Mentor's User Object and Generate Presigned URL for their image
        User mentor = userRepository.findUserByNickname(mentorNickname)
                .orElse(null);
        assert mentor != null;  // 멘토가 존재하지 않을 수 없다.

        String mentorImgUrl = generatePreSignedUrlForMentorImage(mentor);

        // Find Last Message and Time
        ChatMessage lastChatMessage = findLastChatMessageByRoomId(roomId);
        String lastMessage = lastChatMessage.getMessage();
        LocalDateTime lastMessageTime = lastChatMessage.getTime();

        return RoomInfoResponse.of(roomId, mentorNickname, mentorImgUrl, lastMessage, lastMessageTime);
    }

    /**
     * 사용자가 '멘토'인 경우
     * '멘티'의 방 정보를 받아온다.
     */
    private RoomInfoResponse findMenteeRoomsByMentor(Room room) {
        // Get MENTEE nickname, and Room id
        String menteeNickname = room.getMenteeNickname();
        String roomId = room.getId();

        // Find Mentee's User Object and Generate Presigned URL for their image
        User mentee = userRepository.findUserByNickname(menteeNickname)
                .orElse(null);
        assert mentee != null;  // 멘토가 존재하지 않을 수 없다.

        String menteeImgUrl = generatePreSignedUrlForMentorImage(mentee);

        // Find Last Message and Time
        ChatMessage lastChatMessage = findLastChatMessageByRoomId(roomId);
        String lastMessage = lastChatMessage.getMessage();
        LocalDateTime lastMessageTime = lastChatMessage.getTime();

        return RoomInfoResponse.of(roomId, menteeNickname, menteeImgUrl, lastMessage, lastMessageTime);
    }

    private String generatePreSignedUrlForMentorImage(User mentor) {
        // 주의! 만료 기간은 최대 7일까지 설정 가능하다.
        int AWS_URL_DURATION = 7;

        return String.valueOf(awsS3Handler.generatePresignedUrl(
                BUCKET_NAME, mentor.getImgUrl(), Duration.ofDays(AWS_URL_DURATION)
        ));
    }

    protected ChatMessage findLastChatMessageByRoomId(String roomId) {
        int GET_ROOM_INFO_SIZE = 1;

        PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id") // if time is the same, order by _id(because ignore milliseconds in time)
        ));
        List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomId, pageRequest);
        return messagePage.get(0); // Assumes there is at least one message, add error handling if needed
    }

    private List<RoomInfoResponse> sortByLastMessagedTimeOfHourASC(List<RoomInfoResponse> result) {
        result = result.stream()
                .sorted(Comparator.comparing(RoomInfoResponse::getLastMessageTime))
                .collect(Collectors.toList());
        return result;
    }

}
