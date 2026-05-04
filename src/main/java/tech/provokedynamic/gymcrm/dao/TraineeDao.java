package tech.provokedynamic.gymcrm.dao;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.storage.Storage;

@Repository
public class TraineeDaoImpl extends AbstractDao<Trainee> {

    protected TraineeDaoImpl(Storage<Entity> storage) {
        super(storage, "trainee");
    }
}
