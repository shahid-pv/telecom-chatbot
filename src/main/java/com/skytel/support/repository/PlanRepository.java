package com.skytel.support.repository;

import com.skytel.support.entity.Plan;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface PlanRepository extends JpaRepository<Plan, Long> {
	List<Plan> findAllByOrderByPriceAsc();
}
