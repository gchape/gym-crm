package tech.provokedynamic.gymcrm.storage;

public interface KeyFormatter {
    String format(Storage.Namespace namespace, long id);
}
