package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.Request;

public interface TrainingService {

    /**
     * Requirement #16
     */
    void add(Request.AddTraining request);
}
