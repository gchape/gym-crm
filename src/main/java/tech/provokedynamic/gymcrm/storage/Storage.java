package tech.provokedynamic.gymcrm.storage;

import tech.provokedynamic.gymcrm.entity.Entity;

import java.util.Map;

public interface Storage<T extends Entity> {
    void put(Namespace namespace, long id, T entity);

    T get(Namespace namespace, long id);

    void delete(Namespace namespace, long id);

    Map<String, T> getNamespace(Namespace namespace);

    void clear();

    enum Namespace {
        TEST("test"),

        TRAINEE("trainee"),
        TRAINER("trainer"),
        TRAINING("training");

        private final String namespace;

        Namespace(String namespace) {
            this.namespace = namespace;
        }

        public String value() {
            return namespace;
        }
    }
}
