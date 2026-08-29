package tech.provokedynamic.gymcrmworkload.cucumber;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;

public class Hooks {

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Before
    public void resetState() {
        workloadRepository.deleteAll();
    }
}
