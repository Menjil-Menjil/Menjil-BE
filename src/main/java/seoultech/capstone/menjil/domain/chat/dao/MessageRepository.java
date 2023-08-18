package seoultech.capstone.menjil.domain.chat.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    ChatMessage findChatMessageByRoomIdAndMessageType(String roomId, MessageType messageType);

    // totalCount 등의 정보는 필요 없고, 단지 ChatMessage 엔티티 데이터만 가져오면 되므로 Page가 아닌 List로 받도록 작성
    List<ChatMessage> findChatMessageByRoomId(String roomId, Pageable pageable);

}
