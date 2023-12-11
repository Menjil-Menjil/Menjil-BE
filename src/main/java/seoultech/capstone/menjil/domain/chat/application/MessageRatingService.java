package seoultech.capstone.menjil.domain.chat.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dao.MessageRepository;
import seoultech.capstone.menjil.domain.chat.dao.QaListRepository;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.QaList;
import seoultech.capstone.menjil.domain.chat.dto.request.MessageClickIncViewsAndLikesRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageClickIncViewsAndLikesResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageRatingService {

    private final MessageRepository messageRepository;
    private final QaListRepository qaListRepository;

    public MessageClickIncViewsAndLikesResponse incrementViewsAndLikes(MessageClickIncViewsAndLikesRequest request) {
        MessageType ratingType = MessageType.AI_SUMMARY_RATING;
        ChatMessage message = messageRepository.findBy_idAndMessageType(request.getId(), ratingType)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_EXISTED));

        QaList qaList = qaListRepository.findBy_id(request.getQuestionId())
                .orElseThrow(() -> new CustomException(ErrorCode.QALIST_NOT_EXISTED));
        updateViewsAndLikes(qaList, request.getLikeStatus());

        // 변경된 내용 저장: Spring Data MongoDB는 dirty checking이 없다.
        qaListRepository.save(qaList);

        // 평가 이후 AI_SUMMARY_RATING 메시지 제거
        messageRepository.delete(message);

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
