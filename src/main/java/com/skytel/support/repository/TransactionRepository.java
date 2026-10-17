package com.skytel.support.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skytel.support.entity.Transaction;

/** Provides read-only queries for customer transactions. */
@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	/** Finds one customer's transactions between the given timestamps. */
	@Query("select t from Transaction t where t.customerId=:customerId and t.createdAt between :from and :to order by t.createdAt desc")
	List<Transaction> byDate(@Param("customerId") Long customerId, @Param("from") OffsetDateTime from,
			@Param("to") OffsetDateTime to);
}
