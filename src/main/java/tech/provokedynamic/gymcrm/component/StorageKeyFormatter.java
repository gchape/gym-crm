package tech.provokedynamic.gymcrm.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.storage.KeyFormatter;
import tech.provokedynamic.gymcrm.storage.Storage;

@Component
public class StorageKeyFormatter implements KeyFormatter {
    private final String keyFormat;

    public StorageKeyFormatter(@Value("${storage.key.format}") String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String format(Storage.Namespace namespace, long id) {
        return keyFormat.formatted(namespace.value(), id);
    }
}
