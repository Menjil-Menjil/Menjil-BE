package seoultech.capstone.menjil.domain.chatbot.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // To use MySQL
@ActiveProfiles("test")
@DataJpaTest
class ChatBotRoomRepositoryTest {

    @Autowired
    private ChatBotRoomRepository chatBotRoomRepository;

    @Test
    @DisplayName("roomId로 챗봇 대화방을 조회한다")
    void findChatBotRoomByRoomId() {
        // given
        String targetRoomId = "room-1";
        ChatBotRoom room1 = createChatBotRoom(targetRoomId, "i1", "r1");
        ChatBotRoom room2 = createChatBotRoom("room-2", "i2", "r2");
        chatBotRoomRepository.saveAll(List.of(room1, room2));

        // when
        Optional<ChatBotRoom> result = chatBotRoomRepository.findChatBotRoomByRoomId(targetRoomId);

        // then
        // 2개의 데이터가 저장이 잘 되었는지 검증
        assertThat(chatBotRoomRepository.findAll().size()).isEqualTo(2);

        // 조회 검증
        assertThat(result.get().getRoomId()).isEqualTo(targetRoomId);
    }

    @Test
    @DisplayName("사용자 닉네임으로 방 전체를 조회한다.")
    void findAllByInitiatorNickname() {
        // given
        String targetNickname = "i1";
        ChatBotRoom room1 = createChatBotRoom("room-1", targetNickname, "r1");
        ChatBotRoom room2 = createChatBotRoom("room-2", targetNickname, "r2");
        ChatBotRoom room3 = createChatBotRoom("room-3", "i2", "r2");
        chatBotRoomRepository.saveAll(List.of(room1, room2, room3));

        // when
        List<ChatBotRoom> rooms = chatBotRoomRepository.findAllByInitiatorNickname(targetNickname);

        // then
        assertThat(rooms).hasSize(2)
                .extracting("roomId", "initiatorNickname", "recipientNickname")
                .containsExactlyInAnyOrder(
                        tuple("room-1", targetNickname, "r1"),
                        tuple("room-2", targetNickname, "r2")
                );
    }

    @Test
    @DisplayName("roomId로 채팅방을 삭제한다")
    void deleteChatBotRoomByRoomId() {
        // given
        String targetRoomId = "room-1";
        ChatBotRoom room1 = createChatBotRoom(targetRoomId, "i1", "r1");
        ChatBotRoom room2 = createChatBotRoom("room-2", "i2", "r2");
        chatBotRoomRepository.saveAll(List.of(room1, room2));

        // when
        chatBotRoomRepository.deleteChatBotRoomByRoomId(targetRoomId);

        // then
        assertThat(chatBotRoomRepository.findAll().size()).isEqualTo(1);
        assertThat(chatBotRoomRepository.findChatBotRoomByRoomId(targetRoomId)).isEmpty();
    }


    private ChatBotRoom createChatBotRoom(String roomId,
                                          String initiatorNickname,
                                          String recipientNickname) {
        return ChatBotRoom.builder()
                .roomId(roomId)
                .initiatorNickname(initiatorNickname)
                .recipientNickname(recipientNickname)
                .build();
    }
}