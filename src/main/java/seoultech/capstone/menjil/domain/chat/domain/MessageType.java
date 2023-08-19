package seoultech.capstone.menjil.domain.chat.domain;

public enum MessageType {
    /**
     * MessageType 으로 메시지 순서를 정의한다.
     * ENTER: 채팅방 생성 시 맨 처음으로 보내지는 대화(Welcome Message)
     * TALK: 사용자의 자유 대화
     * QUIT: 채팅방 퇴장
     */
    ENTER, QUESTION, AI_QUESTION_RESPONSE, TALK, QUIT
}