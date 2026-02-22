package com.hisabkitab.repository;

import com.hisabkitab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Interface for database operations on the "users" table.
 *
 * By extending JpaRepository, Spring Data JPA automatically provides:
 *   - save()       → INSERT or UPDATE
 *   - findById()   → SELECT by primary key
 *   - findAll()    → SELECT * (all records)
 *   - deleteById() → DELETE by primary key
 *   - count()      → COUNT(*)
 *   ... and many more!
 *
 * We only need to define CUSTOM query methods (like findByEmail).
 * Spring generates the SQL from the method name automatically!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring Data JPA reads this method name and generates:
     * SELECT * FROM users WHERE email = ?
     *
     * Returns Optional because the user might not exist.
     */
    Optional<User> findByEmail(String email);

    /**
     * Spring Data JPA generates:
     * SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
     * FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}
