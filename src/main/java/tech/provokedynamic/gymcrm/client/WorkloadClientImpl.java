package tech.provokedynamic.gymcrm.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tech.provokedynamic.gymcrm.filter.TransactionIdFilter;
import tech.provokedynamic.gymcrm.security.JwtService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadClientImpl implements WorkloadClient {

    private static final String SERVICE_SUBJECT = "gym-crm-service";
    private static final String WORKLOAD_URI = "http://gym-crm-workload/api/trainers/workload";

    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final JwtService jwtService;

    @Override
    @CircuitBreaker(name = "workloadService", fallbackMethod = "sendWorkloadFallback")
    public void sendWorkload(WorkloadRequest request) {
        String token = jwtService.generateToken(SERVICE_SUBJECT);
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

    private void sendWorkloadFallback(WorkloadRequest request, Throwable throwable) {
        log.error("Workload update failed for trainer '{}' (action={}): {}",
                request.trainerUsername(), request.actionType(), throwable.getMessage());
    }
}
