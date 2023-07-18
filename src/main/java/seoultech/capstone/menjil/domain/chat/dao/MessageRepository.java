package seoultech.capstone.menjil.domain.chat.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import seoultech.capstone.menjil.domain.chat.domain.Message;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
}
