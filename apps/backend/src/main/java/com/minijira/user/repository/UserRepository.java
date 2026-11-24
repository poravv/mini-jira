package com.minijira.user.repository;

import com.minijira.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            select u from User u
            where lower(u.email) = lower(:identifier)
               or lower(u.username) = lower(:identifier)
            """)
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    @Query("""
            select u from User u
            where (:active is null or u.isActive = :active)
            order by u.lastName asc, u.firstName asc, u.username asc
            """)
    List<User> search(@Param("active") Boolean active);
}
