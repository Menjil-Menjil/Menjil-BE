package seoultech.capstone.menjil.domain.chat.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    /**
     * MessageType 으로 메시지 순서를 정의한다.
     * ENTER: 채팅방 생성 시 맨 처음으로 보내지는 대화(Welcome Message)
     * QUESTION: 사용자의 질문
     * AI_* : AI와 관련된 기능
     * TYPE_NOT_EXISTS : enum에 정의되어 있지 않은 타입이 들어올 경우, MessageController에서 예외 처리를 위해 생성
     */
    ENTER,
    QUESTION,
    AI_QUESTION_RESPONSE, AI_SUMMARY_LIST, AI_SUMMARY, AI_ANSWER, AI_RATING,
    SELECT,
    TYPE_NOT_EXISTS;
    
    @JsonCreator
    public static MessageType forValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.toString().equals(value)) {
                return type;
            }
        }
        return TYPE_NOT_EXISTS;
    }

    @JsonValue
    public String toValue() {
        return this.toString();
    }
}