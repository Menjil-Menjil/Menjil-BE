package seoultech.capstone.menjil.domain.chat.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    private final int GET_ROOM_INFO_SIZE = 1;

    @AfterEach
    void resetData() {
        // delete mongodb manually
        messageRepository.deleteAll();
    }

    /**
     * findChatMessageByRoomId()
     */
    @Test
    @DisplayName("Pageable을 사용하여 실제로 가장 마지막에 대화한 메시지가 리턴되는지 검증")
    void findChatMessageByRoomId() {
        // given
        List<ChatMessage> chatMessageList = new ArrayList<>();
        int FIXED_NUM = 47;
        String roomId = "fixed_room_id";
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= FIXED_NUM; i++) {
            String _id = "id_" + i;
            SenderType senderType;
            String senderNickname;
            if (i % 2 == 0) {
                senderType = SenderType.MENTOR;
                senderNickname = "mentor_" + i;
            } else {
                senderType = SenderType.MENTEE;
                senderNickname = "mentee_" + i;
            }
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time = now.plusSeconds(i * 1000L);

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(roomId)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id")
        ));
        List<ChatMessage> result = messageRepository.findChatMessageByRoomId(roomId, pageRequest);

        // then
        assertThat(result.size()).isEqualTo(1);
        ChatMessage lastMessage = result.get(0);

        // MongoDB에 가장 나중에 들어간 메시지가, 가장 마지막에 작성된 메시지일 것이므로, 위에서 리스트 형식으로 add() 하는 것은 문제가 없다고 생각함
        // 시간순으로 맨 마지막에 입력된 데이터가 정상적으로 반환되는지 확인
        assertThat(lastMessage.get_id()).isEqualTo("id_" + FIXED_NUM);
        assertThat(lastMessage.getMessage()).isEqualTo("message_" + FIXED_NUM);

        // 원활한 비교를 위해 milliseconds 무시
        assertThat(lastMessage.getTime()).isAfterOrEqualTo(now.plusSeconds(FIXED_NUM * 1000L).withNano(0));
    }

    @Test
    @DisplayName("위의 테스트에서는 시간 순대로 넣는 반면, 여기서는 for문 중간에 시간이 가장 큰 데이터를 임의로 하나 집어넣어서, " +
            "실제로 이 데이터가 리턴되는지 검증")
    void findChatMessageByRoomId_2() {
        // given
        // 테스트 데이터 생성하여 db에 저장
        List<ChatMessage> chatMessageList = new ArrayList<>();
        int FIXED_NUM = 47;
        String roomId = "fixed_room_id";
        LocalDateTime now = LocalDateTime.now();

        int NUM = 33;

        for (int i = 1; i <= FIXED_NUM; i++) {
            String _id = "id_" + i;
            SenderType senderType;
            String senderNickname;
            if (i % 2 == 0) {
                senderType = SenderType.MENTOR;
                senderNickname = "mentor_" + i;
            } else {
                senderType = SenderType.MENTEE;
                senderNickname = "mentee_" + i;
            }
            String message = "message_" + i;
            MessageType messageType = MessageType.TALK;
            LocalDateTime time;

            // 여기서 임의로 i == 33 인 경우에, 시간이 큰 값을 임의로 넣는다.
            if (i == NUM) {
                time = now.plusSeconds(NUM * 1000 * 1000L);
            } else {
                time = now.plusSeconds(i * 1000L);
            }

            // Add list
            chatMessageList.add(ChatMessage.builder()
                    ._id(_id)
                    .roomId(roomId)
                    .senderType(senderType)
                    .senderNickname(senderNickname)
                    .message(message)
                    .messageType(messageType)
                    .time(time)
                    .build());
        }
        messageRepository.saveAll(chatMessageList);

        // when
        PageRequest pageRequest = PageRequest.of(0, GET_ROOM_INFO_SIZE, Sort.by(
                Sort.Order.desc("time"),
                Sort.Order.desc("_id")
        ));
        List<ChatMessage> result = messageRepository.findChatMessageByRoomId(roomId, pageRequest);

        // then
        assertThat(result.size()).isEqualTo(1);
        ChatMessage lastMessage = result.get(0);

        assertThat(lastMessage.get_id()).isEqualTo("id_" + NUM);
        assertThat(lastMessage.getMessage()).isEqualTo("message_" + NUM);

        // 원활한 비교를 위해 milliseconds 무시
        assertThat(lastMessage.getTime()).isAfterOrEqualTo(now.plusSeconds(NUM * 1000 * 1000L).withNano(0));
    }
}