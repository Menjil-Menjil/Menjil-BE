package seoultech.capstone.menjil.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.Room;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class RoomDto {

    @NotBlank
    private String menteeNickname;
    @NotBlank
    private String mentorNickname;

    @Builder(builderMethodName = "roomDtoConstructor")
    private RoomDto(String menteeNickname, String mentorNickname) {
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
    }

    @Builder
    public Room toRoom(String roomId) {
        return Room.builder()
                .roomId(roomId)
                .menteeNickname(menteeNickname)
                .mentorNickname(mentorNickname)
                .build();
    }

    public static RoomDto fromRoom(Room room) {
        return RoomDto.roomDtoConstructor()
                .menteeNickname(room.getMenteeNickname())
                .mentorNickname(room.getMentorNickname())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomDto roomDto = (RoomDto) o;
        return Objects.equals(menteeNickname, roomDto.menteeNickname)
                && Objects.equals(mentorNickname, roomDto.mentorNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menteeNickname, mentorNickname);
    }
}
