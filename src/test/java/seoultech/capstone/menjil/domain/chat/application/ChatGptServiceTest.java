package seoultech.capstone.menjil.domain.chat.application;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import seoultech.capstone.menjil.domain.chat.dto.Message;
import seoultech.capstone.menjil.domain.chat.dto.request.ChatGptRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.ChatGptResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
@ActiveProfiles("test")
class ChatGptServiceTest {

    private WebClient webClient;
    private static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private String gptJsonResponse;

    @Value("${openai.model}")
    private String OPEN_AI_MODEL;

    @BeforeEach
    public void setUp() {
        wireMockServer.start();
        String baseUrl = String.format("http://localhost:%s", wireMockServer.port());
        this.webClient = WebClient.builder().baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        gptJsonResponse = "{\n" +
                "  \"id\": \"chatcmpl-123\",\n" +
                "  \"object\": \"chat.completion\",\n" +
                "  \"created\": 1677652288,\n" +
                "  \"choices\": [{\n" +
                "    \"index\": 0,\n" +
                "    \"message\": {\n" +
                "      \"role\": \"assistant\",\n" +
                "      \"content\": \"Hello there, how may I assist you today?\"\n" +
                "    },\n" +
                "    \"finish_reason\": \"stop\"\n" +
                "  }],\n" +
                "  \"usage\": {\n" +
                "    \"prompt_tokens\": 9,\n" +
                "    \"completion_tokens\": 12,\n" +
                "    \"total_tokens\": 21\n" +
                "  }\n" +
                "}";
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    /**
     * getMessageFromGpt()
     */
    @Test
    @DisplayName("정상적으로 요청이 수행되는 경우")
    void getMessageFromGpt() {
        // given
        String question = "You are a helpful assistant.";
        String msg = "내 질문은 \"" + question + "\" 이야. " + "이 질문을 세 줄 요약해줘";
        ChatGptRequest gptRequest = ChatGptRequest
                .builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(Message.builder()
                        .role("user")
                        .content(msg)
                        .build()))
                .build();
        Gson gson = new Gson();
        String gptRequestStr = gson.toJson(gptRequest);

        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/"))
                        .withRequestBody(equalToJson(gptRequestStr))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody(gptJsonResponse)));

        ChatGptService chatGptService = new ChatGptService(webClient);

        // when
        Message result = chatGptService.getMessageFromGpt(question);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("assistant");
        assertThat(result.getContent()).isEqualTo("Hello there, how may I assist you today?");
    }

    @Test
    @DisplayName("ChatGPT API를 통해 받은 데이터가 null이면, CustomException 리턴")
    void messageIsNull() {
        // given
        String question = "You are a helpful assistant.";
        String msg = "내 질문은 \"" + question + "\" 이야. " + "이 질문을 세 줄 요약해줘";
        ChatGptRequest gptRequest = ChatGptRequest
                .builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(Message.builder()
                        .role("user")
                        .content(msg)
                        .build()))
                .build();
        Gson gson = new Gson();
        String gptRequestStr = gson.toJson(gptRequest);

        gptJsonResponse = null;

        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/"))
                        .withRequestBody(equalToJson(gptRequestStr))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody(gptJsonResponse)));

        ChatGptService chatGptService = new ChatGptService(webClient);

        // when
        assertThrows(CustomException.class, () -> chatGptService.getMessageFromGpt(question));
    }

    /**
     * sendRequestToGpt()
     */
    @Test
    @DisplayName("ChatGPT API 테스트")
    void sendRequestToGpt() {
        // given
        ChatGptRequest gptRequest = ChatGptRequest
                .builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(Message.builder()
                        .role("user")
                        .content("You are a helpful assistant.")
                        .build()))
                .build();
        Gson gson = new Gson();
        String gptRequestStr = gson.toJson(gptRequest);

        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/"))
                        .withRequestBody(equalToJson(gptRequestStr))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody(gptJsonResponse)));

        ChatGptService chatGptService = new ChatGptService(webClient);
        ChatGptResponse responseMono = chatGptService.sendRequestToGpt(gptRequest).block();

        // then
        assertThat(responseMono).isNotNull();
        assertThat(responseMono.getId()).isEqualTo("chatcmpl-123");
        assertThat(responseMono.getChoices().get(0).getMessage().getContent())
                .isEqualTo("Hello there, how may I assist you today?");
        assertThat(responseMono.getUsage().getTotalTokens()).isEqualTo(21);
    }

}