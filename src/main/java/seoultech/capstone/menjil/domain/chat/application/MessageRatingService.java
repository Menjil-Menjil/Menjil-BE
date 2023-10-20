package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageClickIncViewsAndLikesRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageClickIncViewsAndLikesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageRatingService {

    private final QaListRepository qaListRepository;

    public MessageClickIncViewsAndLikesResponse incrementViewsAndLikes(MessageClickIncViewsAndLikesRequest request) {
        QaList qaList = qaListRepository.findBy_id(request.getQuestionId())
                .orElseThrow(() -> new CustomException(ErrorCode.QALIST_NOT_EXISTED));
        updateViewsAndLikes(qaList, request.getLikeStatus());

        // 변경된 내용 저장: Spring Data MongoDB는 dirty checking이 없다.
        qaListRepository.save(qaList);

        return MessageClickIncViewsAndLikesResponse.of(qaList.get_id(),
                qaList.getViews(), qaList.getLikes());
    }

    private void updateViewsAndLikes(QaList qaList, Boolean bLikeStatus) {
        int updateNum = 1;
        qaList.setViews(qaList.getViews() + updateNum);
        if (bLikeStatus) {
            qaList.setLikes(qaList.getLikes() + updateNum);
        }
    }
}