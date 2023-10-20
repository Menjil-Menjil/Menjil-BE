package seoultech.capstone.menjil.domain.chat.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import seoultech.capstone.menjil.domain.chat.dto.request.AwsLambdaRequest;
import seoultech.capstone.menjil.domain.chat.dto.response.AwsLambdaResponse;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class AwsLambdaClient {

    private final WebClient apiGatewayClient;

    @Autowired
    public AwsLambdaClient(@Qualifier("apiGatewayClient") WebClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    public List<AwsLambdaResponse> sendRequestToLambda(AwsLambdaRequest awsLambdaRequest) {
        try {
            return apiGatewayClient.post()
                    .uri("/api/lambda/question")
                    .body(BodyInserters.fromValue(awsLambdaRequest))
                    .retrieve()
                    .bodyToFlux(AwsLambdaResponse.class)
                    .timeout(Duration.ofSeconds(180))
                    .onErrorResume(e -> {
                        log.error("An error occurred while fetching from Lambda", e);
                        return Mono.empty();
                    })
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error(">> An exception occurred while making the AWS Lambda request", e);
            return null;
        }
    }
}