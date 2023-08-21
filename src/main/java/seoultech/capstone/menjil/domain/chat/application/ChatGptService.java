package seoultech.capstone.menjil.domain.chat.application;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.request.ChatGptRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.ChatGptResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.util.List;

@Slf4j
@Service
public class ChatGptService {

    private final WebClient chatGptWebClient;

    public ChatGptService(@Qualifier("chatGptWebClient") WebClient chatGptWebClient) {
        this.chatGptWebClient = chatGptWebClient;
    }

    @Value("${openai.model}")
    private String OPEN_AI_MODEL;

    /**
     * 질문 데이터를 받아서 세 줄 요약을 수행
     */
    public Message getMessageFromGpt(String question) {
        String msg = "내 질문은 \"" + question + "\" 이야. " + "이 문장을 세 줄로 요약해줘";

        ChatGptRequest chatGptRequest = ChatGptRequest
                .builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(Message.builder()
                        .role("user")
                        .content(msg)
                        .build()))
                .build();

        ChatGptResponse response = sendRequestToGpt(chatGptRequest).block();

        if (response != null) {
            return Message.builder()
                    .role(response.getChoices().get(0).getMessage().getRole())
                    .content(response.getChoices().get(0).getMessage().getContent())
                    .build();
        } else {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    // to use baseUrl in parameter because of test code.
    public Mono<ChatGptResponse> sendRequestToGpt(ChatGptRequest gptRequest) {
        Gson gson = new Gson();
        String jsonToStr = gson.toJson(gptRequest);

        return chatGptWebClient.post()
                .body(BodyInserters.fromValue(jsonToStr))
                .retrieve()
                .bodyToMono(ChatGptResponse.class);

        /*WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPEN_AI_SECRET_KEY)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.post()
                .body(BodyInserters.fromValue(jsonToStr))
                .retrieve()
                .bodyToMono(ChatGptResponse.class);*/
    }

}
