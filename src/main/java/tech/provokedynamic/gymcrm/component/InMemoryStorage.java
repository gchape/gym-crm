package tech.provokedynamic.gymcrm.component;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.storage.KeyFormatter;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryStorage implements Storage<Entity> {
    private final KeyFormatter keyFormatter;

    private final Map<String, Entity> storage = new ConcurrentHashMap<>();

    public InMemoryStorage(KeyFormatter keyFormatter) {
        this.keyFormatter = keyFormatter;
    }

    @Override
    public void put(Namespace namespace, long id, Entity entity) {
        var key = keyFormatter.format(namespace, id);

        storage.put(key, entity);
    }

    @Override
    public Entity get(Namespace namespace, long id) {
        var key = keyFormatter.format(namespace, id);

        return storage.get(key);
    }

    @Override
    public void delete(Namespace namespace, long id) {
        var key = keyFormatter.format(namespace, id);

        storage.remove(key);
    }

    @Override
    public Map<String, Entity> getNamespace(Namespace namespace) {
        String prefix = namespace.value() + ":";

        return storage.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        e -> e.getKey().substring(prefix.length()),
                        Map.Entry::getValue
                ));
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
