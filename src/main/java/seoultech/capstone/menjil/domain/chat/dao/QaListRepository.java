package seoultech.capstone.menjil.domain.chat.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;

import java.util.List;

@Repository
public interface QaListRepository extends MongoRepository<QaList, String> {

    @Query(value = "{ 'mentor_nickname' : ?0, 'answer' : { '$ne' : null } }", fields = "{ 'question_summary' : 1 }")
    List<QaList> findAnsweredQuestionsByMentor(String mentorNickname, Pageable pageable);

}
