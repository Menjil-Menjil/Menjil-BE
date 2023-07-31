package seoultech.capstone.menjil.domain.chat.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.request.ChatGptRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.ChatGptResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatGptService {

    private final Gson gson;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String OPEN_AI_MODEL;

    @Value("${openai.api.url}")
    private String OPEN_AI_URL;

    @Value("${openai.api.secret-key}")
    private String OPEN_AI_SECRET_KEY;


    /**
     * 질문 데이터를 받아서 세 줄 요약을 수행
     */
    public Message getMessageFromGpt(String question) {
        String msg = "내 질문은 \"" + question + "\" 이야. " + "이 질문을 세 줄 요약해줘";

        ChatGptRequest chatGptRequest = ChatGptRequest
                .builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(Message.builder()
                        .role("user")
                        .content(msg)
                        .build()))
                .build();

        String response = sendRequestToGpt(chatGptRequest);

        try {
            ChatGptResponse chatGptResponse = objectMapper.readValue(response, ChatGptResponse.class);

            return Message.builder()
                    .role(chatGptResponse.getChoices().get(0).getMessage().getRole())
                    .content(chatGptResponse.getChoices().get(0).getMessage().getContent())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("[[ChatGptService]] sendRequestToGpt ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    public String sendRequestToGpt(ChatGptRequest gptRequest) {
        String jsonToStr = gson.toJson(gptRequest);
        String response;

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPEN_AI_URL))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer " + OPEN_AI_SECRET_KEY)
                    .POST(BodyPublishers.ofString(jsonToStr))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            log.error("[[ChatGptService]] sendRequestToGpt ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
        return response;
    }

}
