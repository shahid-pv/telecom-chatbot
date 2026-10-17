package com.skytel.support.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skytel.support.entity.Customer;

/** Provides read-only queries for customer accounts. */
@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	/** Loads all customers for the demo account picker. */
	@Query("select c from Customer c order by c.id")
	List<Customer> findAllReadOnly();

	/** Finds one customer without opening a write transaction. */
	@Query("select c from Customer c where c.id=:id")
	Optional<Customer> findByIdReadOnly(@Param("id") Long id);
}
