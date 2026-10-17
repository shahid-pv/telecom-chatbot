package com.skytel.support.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skytel.support.entity.ActiveService;

/** Provides read-only queries for services attached to customer accounts. */
@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface ActiveServiceRepository extends JpaRepository<ActiveService, Long> {
	/** Lists active services for the requested customer. */
	@Query("select s from ActiveService s where s.customerId=:id and s.status='ACTIVE'")
	List<ActiveService> activeFor(@Param("id") Long id);
}
