package tech.provokedynamic.gymcrm.validation;

@FunctionalInterface
public interface Validator<T> {

    void validate(T obj);
}
