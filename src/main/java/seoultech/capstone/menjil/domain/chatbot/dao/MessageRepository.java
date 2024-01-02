package seoultech.capstone.menjil.domain.chatbot.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chatbot.domain.MessageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    Optional<ChatMessage> findBy_idAndMessageType(String _id, MessageType type);

    // totalCount 등의 정보는 필요 없고, 단지 ChatMessage 엔티티 데이터만 가져오면 되므로 Page가 아닌 List로 받도록 작성
    List<ChatMessage> findChatMessageByRoomId(String roomId, Pageable pageable);

    // 테스트를 위한 메서드
    Optional<ChatMessage> findByRoomId(String roomId);

    void deleteChatMessagesByRoomId(String roomId);

}
