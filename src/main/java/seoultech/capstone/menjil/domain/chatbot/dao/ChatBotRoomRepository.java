package seoultech.capstone.menjil.domain.chatbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;

import java.util.List;
import java.util.Optional;

public interface ChatBotRoomRepository extends JpaRepository<ChatBotRoom, Long> {

    Optional<ChatBotRoom> findChatBotRoomByRoomId(String roomId);

    List<ChatBotRoom> findAllByInitiatorNickname(String initiatorNickname);

    void deleteChatBotRoomByRoomId(String roomId);

}
