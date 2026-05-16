package tech.provokedynamic.gymcrm.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@lombok.Getter
@DiscriminatorValue("trainee")
@lombok.experimental.SuperBuilder(toBuilder = true)
@lombok.NoArgsConstructor(access = AccessLevel.PROTECTED)
@org.hibernate.annotations.OnDelete(action = OnDeleteAction.CASCADE)
public class Trainee extends User {
    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private final Set<Trainer> trainers = new HashSet<>();

    @Nullable
    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Nullable
    @Embedded
    private Address address;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(getId(), trainee.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
