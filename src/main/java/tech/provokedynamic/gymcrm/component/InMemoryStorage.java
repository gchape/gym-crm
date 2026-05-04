package tech.provokedynamic.gymcrm.storage;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryStorage implements Storage<Entity> {
    private final Map<String, Entity> storage = new ConcurrentHashMap<>();

    @Override
    public void put(String namespace, long id, Entity entity) {
        var key = toKeyFn.apply(namespace, id);

        storage.put(key, entity);
    }

    @Override
    public Entity get(String namespace, long id) {
        var key = toKeyFn.apply(namespace, id);

        return storage.get(key);
    }

    @Override
    public void delete(String namespace, long id) {
        var key = toKeyFn.apply(namespace, id);

        storage.remove(key);
    }

    @Override
    public Map<String, Entity> getNamespace(String namespace) {
        return storage.entrySet()
                .stream()
                .<Map.Entry<String, Entity>>mapMulti((entry, upstream) -> {
                    if (entry.getKey().startsWith(namespace + ":")) {
                        int beginIndex = namespace.length() + 1;
                        var id = entry.getKey().substring(beginIndex);

                        upstream.accept(Map.entry(id, entry.getValue()));
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
