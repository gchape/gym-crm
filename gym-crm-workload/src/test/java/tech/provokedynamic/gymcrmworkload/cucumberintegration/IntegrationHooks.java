package tech.provokedynamic.gymcrmworkload.cucumberintegration;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;

public class IntegrationHooks {

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Before
    public void resetState() {
        workloadRepository.deleteAll();
    }
}
