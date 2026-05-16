package tech.provokedynamic.gymcrm.dao;

import org.hibernate.annotations.processing.HQL;
import org.hibernate.annotations.processing.SQL;

public interface UserDao {

    @SQL("""
            SELECT count(*) > 0
            FROM "user"
            WHERE username = :username
            """)
    boolean existsByUsernameIncludingDeleted(String username);

    @HQL("""
            SELECT count(u.id) > 0
            FROM User u
            WHERE u.username = :username
                AND u.password = :password
            """)
    boolean existsByUsernameAndPassword(String username, String password);

    @HQL("""
            UPDATE User u
            SET u.password = :newPassword
            WHERE u.username = :username
            """)
    void updatePassword(String username, String newPassword);

    @SQL("""
            UPDATE "user"
            SET is_active = false
            WHERE username = :username
              AND is_active = true
            """)
    int deactivateByUsername(String username);

    @SQL("""
            UPDATE "user"
            SET is_active = true
            WHERE username = :username
              AND is_active = false
            """)
    int activateByUsername(String username);
}
