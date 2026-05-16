package tech.provokedynamic.gymcrm.dao;

import tech.provokedynamic.gymcrm.entity.TrainingType;

public interface TrainingTypeDao {

    TrainingType findByName(String name);
}
