package tech.provokedynamic.gymcrm.dao;

import org.hibernate.annotations.processing.HQL;
import tech.provokedynamic.gymcrm.entity.TrainingType;

public interface TrainingTypeDao {

    @HQL("""
            SELECT tt
            FROM TrainingType tt
            WHERE tt.trainingTypeName = :name
            """)
    TrainingType findByName(String name);
}
