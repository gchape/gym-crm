package tech.provokedynamic.gymcrm.validation;

/** Validates input before an application operation mutates persistent state. */
@FunctionalInterface
public interface InputValidator<T> {

    void validate(T obj);
}
