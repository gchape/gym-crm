package tech.provokedynamic.gymcrm.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.filter.TransactionIdFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadClientImpl implements WorkloadClient {

    private static final String WORKLOAD_URI = "http://gym-crm-workload/api/trainers/workload";
    private static final String REGISTRATION_ID = "gym-crm-service";

    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    @CircuitBreaker(name = "workloadService", fallbackMethod = "sendWorkloadFallback")
    public void sendWorkload(Request.WorkloadRequest request) {
        var authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(REGISTRATION_ID)
                .principal(REGISTRATION_ID)
                .build();

        var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        var token = authorizedClient.getAccessToken().getTokenValue();

        String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);

        loadBalancedRestClientBuilder.build()
                .post()
                .uri(WORKLOAD_URI)
                .header("Authorization", "Bearer " + token)
                .header(TransactionIdFilter.TRANSACTION_ID_HEADER, transactionId != null ? transactionId : "")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Workload update sent - trainer=[{}], action=[{}]", request.trainerUsername(), request.actionType());
    }

    private void sendWorkloadFallback(Request.WorkloadRequest request, Throwable throwable) {
        log.error("Workload update failed for trainer '{}' (action={}): {}",
                request.trainerUsername(), request.actionType(), throwable.getMessage());
    }
}
