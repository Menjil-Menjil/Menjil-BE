package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RoomInfo {

    private String roomId;
    private String nickname;
    private String imgUrl;
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // to send LocalDateTime format more pretty
    private LocalDateTime lastMessageTime;

    @Builder
    private RoomInfo(String roomId, String nickname, String imgUrl, String lastMessage,
                     LocalDateTime lastMessageTime) {
        this.roomId = roomId;
        this.nickname = nickname;
        this.imgUrl = imgUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
}
