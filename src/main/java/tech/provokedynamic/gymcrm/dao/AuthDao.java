package tech.provokedynamic.gymcrm.dao;

import org.hibernate.annotations.processing.HQL;

public interface AuthDao {

    @HQL("""
            SELECT count(u.id) > 0
            FROM User u
            WHERE u.username = :username
                AND u.password = :password
            """)
    boolean existsByUsernameAndPassword(String username, String password);
}
