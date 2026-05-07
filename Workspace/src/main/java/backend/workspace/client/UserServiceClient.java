package backend.workspace.client;

import backend.workspace.dto.User.UserBatchSummaryRequest;
import backend.workspace.dto.User.UserSummaryResponse;
import backend.workspace.exception.UserService.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient restClient;
    private final String batchSummaryEndpoint;

    public UserServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${user.service.url:http://user-service:8080}") String userServiceUrl,
            @Value("${user.service.batch-summary-endpoint:/internal/users/batch-summary}") String batchSummaryEndpoint
    ) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
        this.batchSummaryEndpoint = batchSummaryEndpoint;
    }

    public List<UserSummaryResponse> getUserSummaries(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        try {
            List<UserSummaryResponse> response = restClient.post()
                    .uri(batchSummaryEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UserBatchSummaryRequest(userIds))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            return response != null ? response : List.of();
        } catch (RestClientException ex) {
            throw new UserServiceUnavailableException("No fue posible obtener usuarios desde user-service", ex);
        }
    }
}
