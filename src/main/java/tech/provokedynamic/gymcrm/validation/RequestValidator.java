package tech.provokedynamic.gymcrm.validation;

import tech.provokedynamic.gymcrm.dto.Request;

public interface ValidatorIF {

    <T extends Request> void validate(T object);
}
