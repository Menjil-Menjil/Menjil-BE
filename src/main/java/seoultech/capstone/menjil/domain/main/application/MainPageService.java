package seoultech.capstone.menjil.domain.main.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.main.dto.response.FollowListResponse;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.handler.AwsS3Handler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainPageService {

    private final AwsS3Handler awsS3Handler;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final QaListRepository qaListRepository;

    private final int GET_ROOM_INFO_SIZE = 1;
    private final int AWS_URL_DURATION = 7;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 멘토 리스트를 가져오는 메서드
     * nickname은, 추후 멘토 추천 알고리즘 사용시 필요할 수 있으므로, 우선 받도록 하였으나.
     * 현재 수행하는 기능은 없다.
     */
    public Page<MentorInfoResponse> getMentorList(String nickname, Pageable pageable) {
        Page<User> page = userRepository.findUsersByRole(UserRole.MENTOR, pageable);
        Page<MentorInfoResponse> mentorInfoResponse = page.map(user -> {
            MentorInfoResponse dto = MentorInfoResponse.fromUserEntity(user);

            // set AWS S3 presigned url
            dto.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                    BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

            // set lastAnsweredMessage
            dto.setLastAnsweredMessage(getLastAnsweredMessages(user.getNickname()));
            return dto;
        });
        return mentorInfoResponse;
    }

    /**
     * 사용자의 채팅방 목록을 가져온다.
     * RoomService의 getAllRooms() 메소드와 유사함
     */
    public List<RoomInfoResponse> getAllRoomsOfUser(String nickname) {
        List<RoomInfoResponse> result = new ArrayList<>();

        User user = userRepository.findUserByNickname(nickname).orElse(null);
        if (user == null) {
            throw new CustomException(ErrorCode.NICKNAME_NOT_EXISTED);
        }
        UserRole role = user.getRole();

        /* 사용자가 멘티인 경우 */
        if (role.equals(UserRole.MENTEE)) {
            // 1. 우선 사용자인 멘티의 닉네임으로 전체 채탕방을 조회한다.
            List<Room> roomList = roomRepository.findRoomsByMenteeNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }
            for (Room room : roomList) {
                result.add(findMentorRoomsByMentee(room));
            }
        }
        /* 사용자가 멘토인 경우 */
        else {
            List<Room> roomList = roomRepository.findRoomsByMentorNickname(nickname);
            if (roomList.isEmpty()) {
                // 채팅방이 하나도 없는 경우
                return result;
            }
            for (Room room : roomList) {
                result.add(findMenteeRoomsByMentor(room));
            }
        }
        result = sortByLastMessagedTimeOfHourDESC(result);
        return result;
    }

    public List<FollowListResponse> getFollowersOfUser(String nickname) {
        List<User> followUsers = followRepository.findFollowsByUserNickname(nickname)
                .stream()
                .map(Follow::getFollowNickname)
                .map(userRepository::findUserByNickname)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // 팔로우를 가장 최근에 한 사용자를 리스트 인덱스 0에 두도록 변경.
        Collections.reverse(followUsers);

        return followUsers.stream()
                .map(user -> {
                    FollowListResponse response = FollowListResponse.fromUserEntity(user);
                    response.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                            BUCKET_NAME, response.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 '멘티'인 경우
     * '멘토'의 방 정보를 받아온다.
     */
    private RoomInfoResponse findMentorRoomsByMentee(Room room) {
        // Get MENTOR nickname, and Room id
        String mentorNickname = room.getMentorNickname();
        String roomId = room.getId();

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

    private List<String> getLastAnsweredMessages(String mentorNickname) {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "question_time", "id")); // Get only the first 2 documents and sort them by 'question_time' and 'id' in descending order
        List<QaList> qaLists = qaListRepository.findAnsweredQuestionsByMentor(mentorNickname, pageable);

        if (qaLists.isEmpty()) {
            return new ArrayList<>();
        } else if (qaLists.size() == 1) {
            return Stream.of(qaLists.get(0))
                    .map(QaList::getQuestionSummary)
                    .collect(Collectors.toList());
        }
        return List.of(qaLists.get(0).getQuestionSummary(), qaLists.get(1).getQuestionSummary());
    }
}
