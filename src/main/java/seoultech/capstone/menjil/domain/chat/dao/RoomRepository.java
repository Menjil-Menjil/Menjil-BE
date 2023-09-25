package seoultech.capstone.menjil.domain.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.chat.domain.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {


    /* 특정 사용자의 특정 방 데이터 가져오기; roomId, menteeId 로 조회 */
    Room findRoomById(String roomId);

    Room findRoomByIdAndMenteeNickname(String roomId, String menteeNickname);

    /* 특정 사용자의 전체 방 리스트 가져오기 */
    List<Room> findRoomsByMenteeNickname(String menteeNickname);

    List<Room> findRoomsByMentorNickname(String mentorNickname);

    void deleteRoomById(String roomId);

}
