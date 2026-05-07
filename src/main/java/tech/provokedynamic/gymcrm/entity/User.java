package tech.provokedynamic.gymcrm.entity;

public abstract class User {
    public abstract String firstName();

    public abstract String lastName();

    public abstract String username();

    public abstract String password();

    public abstract boolean isActive();
}
