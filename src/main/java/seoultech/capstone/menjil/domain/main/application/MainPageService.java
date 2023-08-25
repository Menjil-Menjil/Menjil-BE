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
import seoultech.capstone.menjil.domain.chat.dao.RoomRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.Room;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;
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
public class MainPageService {

    private final AwsS3Handler awsS3Handler;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    private final int GET_ROOM_INFO = 1;
    private final int AWS_URL_DURATION = 7;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 멘토 리스트를 가져오는 메서드
     */
    public Page<MentorInfoResponse> getMentorList(Pageable pageable) {
        Page<User> page = userRepository.findUsersByRole(UserRole.MENTOR, pageable);
        Page<MentorInfoResponse> mentorInfoDto = page.map(user -> {
            MentorInfoResponse dto = MentorInfoResponse.fromUserEntity(user);

            // set AWS S3 presigned url
            dto.setImgUrl(String.valueOf(awsS3Handler.generatePresignedUrl(
                    BUCKET_NAME, user.getImgUrl(), Duration.ofDays(AWS_URL_DURATION))));

            // set lastAnsweredMessage
            // 우선 임의로 작성
            dto.setLastAnsweredMessage("현재 테스트 중입니다");
            return dto;
        });
        return mentorInfoDto;
    }

    /**
     * 사용자의 채팅방 목록을 가져온다.
     * RoomService의 getAllRooms() 메소드와 유사함
     */
    public List<RoomInfoResponse> getUserRoomList(String nickname) {
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
                PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO, Sort.by(
                        Sort.Order.desc("time"),
                        Sort.Order.desc("_id") // if time is same, order by _id(because ignore milliseconds in time)
                ));
                List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomId, pageRequest);
                String lastMessage = messagePage.get(0).getMessage();
                LocalDateTime lastMessageTime = messagePage.get(0).getTime();

                // Calculate last messaged time of Hour (e.g. 2시간 전, ...)
                Long lastMessagedTimeOfHour = timeCalculation(lastMessageTime);

                result.add(RoomInfoResponse.of(roomId, mentorNickname,
                        mentorImgUrl, lastMessage, lastMessagedTimeOfHour));
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
                PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO, Sort.by(
                        Sort.Order.desc("time"),
                        Sort.Order.desc("_id") // if time is same, order by _id(because ignore milliseconds in time)
                ));
                List<ChatMessage> messagePage = messageRepository.findChatMessageByRoomId(roomId, pageRequest);
                String lastMessage = messagePage.get(0).getMessage();
                LocalDateTime lastMessageTime = messagePage.get(0).getTime();

                // Calculate last messaged time of Hour (e.g. 2시간 전, ...)
                Long lastMessagedTimeOfHour = timeCalculation(lastMessageTime);

                result.add(RoomInfoResponse.of(roomId, menteeNickname,
                        menteeImgUrl, lastMessage, lastMessagedTimeOfHour));
            }
        }

        // Sort by getLastMessagedTimeOfHour, order by ASC
        // 가장 최근에 대화한 내용이 있는 대화방이 앞에 오도록 정렬
        result = result.stream()
                .sorted(Comparator.comparing(RoomInfoResponse::getLastMessagedTimeOfHour))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 마지막 대화 내용이, 현재 시간 기준으로 몇 시간 전에 대화하였는지 제공하는 메서드
     * e.g) 2시간 전, 3시간 전, ...
     */
    public Long timeCalculation(LocalDateTime lastMessageTime) {
        Duration duration = Duration.between(lastMessageTime, LocalDateTime.now());
        return duration.toHours();
    }

}
