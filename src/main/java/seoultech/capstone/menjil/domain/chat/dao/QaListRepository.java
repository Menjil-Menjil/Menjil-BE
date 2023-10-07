package seoultech.capstone.menjil.domain.chat.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;

import java.util.List;

@Repository
public interface QaListRepository extends MongoRepository<QaList, String> {

    /*
    이 메서드에서는, question_summary field 외에 다른 필드는 필요하지 않으므로
    쿼리성능 향상을 위해, fields 에서 question_summary 값만 가져오도록 설정하였다.
    But in MongoDb, the _id field is included by default even if you don't explicitly specify it.
    This is just the standard behavior of MongoDB
     */
    @Query(value = "{ 'mentor_nickname' : ?0, 'answer' : { '$ne' : null } }",
            fields = "{ 'question_summary' : 1 }")
    List<QaList> findAnsweredQuestionsByMentor(String mentorNickname, Pageable pageable);

    @Query(value = "{'answer' : { '$ne' : null } }",
            fields = "{ 'question_origin' : 1, 'question_summary' : 1, " +
                    "'answer' :  1, 'answer_time': 1 }")
    List<QaList> findQaListsByMentorNicknameOrderByAnswerTimeAsc(String mentorNickname);

    Long countByMentorNicknameAndAnswerIsNotNull(String mentorNickname);

}
