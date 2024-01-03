package seoultech.capstone.menjil.domain.chatbot.application.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Getter
@NoArgsConstructor
public class ChatBotRoomServiceRequest {

    private String initiatorNickname;
    private String recipientNickname;

    @Builder
    private ChatBotRoomServiceRequest(String initiatorNickname, String recipientNickname) {
        this.initiatorNickname = initiatorNickname;
        this.recipientNickname = recipientNickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatBotRoomServiceRequest that = (ChatBotRoomServiceRequest) o;
        return Objects.equals(initiatorNickname, that.initiatorNickname)
                && Objects.equals(recipientNickname, that.recipientNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiatorNickname, recipientNickname);
    }
}
