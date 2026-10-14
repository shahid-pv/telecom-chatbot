package com.skytel.support.repository;

import com.skytel.support.entity.Customer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	@Query("select c from Customer c")
	List<Customer> findAllReadOnly();

	@Query("select c from Customer c where c.id=:id")
	Optional<Customer> findByIdReadOnly(@Param("id") Long id);
}
