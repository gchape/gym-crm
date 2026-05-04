package tech.provokedynamic.gymcrm.entity;

public class User {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final boolean isActive;

    protected User() {
        this.firstName = null;
        this.lastName = null;
        this.username = null;
        this.password = null;
        this.isActive = false;
    }

    protected User(Builder<?> builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.username = builder.username;
        this.password = builder.password;
        this.isActive = builder.isActive;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return isActive;
    }

    protected abstract static class Builder<B extends Builder<B>> {
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private boolean isActive;

        protected Builder() {
        }

        @SuppressWarnings("unchecked")
        public B firstName(String firstName) {
            this.firstName = firstName;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B lastName(String lastName) {
            this.lastName = lastName;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B username(String username) {
            this.username = username;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B password(String password) {
            this.password = password;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B isActive(boolean isActive) {
            this.isActive = isActive;
            return (B) this;
        }

        public abstract User build();
    }
}
