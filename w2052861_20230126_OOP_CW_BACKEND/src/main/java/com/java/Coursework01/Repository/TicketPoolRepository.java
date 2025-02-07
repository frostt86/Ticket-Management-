package com.java.Coursework01.Repository;

import com.java.Coursework01.Class.TicketPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing `TicketPool` entities.
 * Extends JpaRepository to provide standard CRUD operations and database access.
 */
@Repository // Marks this interface as a Spring Data Repository
public interface TicketPoolRepository extends JpaRepository<TicketPool, Integer> {
    // Inherits methods like save, findById, findAll, delete, etc., from JpaRepository
    // <T, ID> - T is the entity type, and ID is the type of the primary key
}
