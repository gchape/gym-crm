package tech.provokedynamic.gymcrm.client;

import tech.provokedynamic.gymcrm.dto.Request;

public interface WorkloadClient {

    void sendWorkload(Request.WorkloadRequest request);
}
