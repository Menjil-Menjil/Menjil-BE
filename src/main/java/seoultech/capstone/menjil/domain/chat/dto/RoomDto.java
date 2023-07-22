package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.Room;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class RoomDto {

    @NotBlank
    private String roomId;
    @NotBlank
    private String menteeNickname;
    @NotBlank
    private String mentorNickname;

    @Builder(builderMethodName = "roomDtoConstructor")
    public RoomDto(String roomId, String menteeNickname, String mentorNickname) {
        this.roomId = roomId;
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
    }

    @Builder
    public Room toRoom() {
        return Room.builder()
                .roomId(roomId)
                .menteeNickname(menteeNickname)
                .mentorNickname(mentorNickname)
                .build();
    }

    public static RoomDto fromRoom(Room room) {
        return RoomDto.roomDtoConstructor()
                .roomId(room.getRoomId())
                .menteeNickname(room.getMenteeNickname())
                .mentorNickname(room.getMentorNickname())
                .build();
    }
}
