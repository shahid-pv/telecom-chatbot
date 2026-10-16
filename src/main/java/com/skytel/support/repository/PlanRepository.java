package com.skytel.support.repository;

import com.skytel.support.entity.Plan;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/** Provides read-only access to the mobile plans catalog. */
@Repository
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public interface PlanRepository extends JpaRepository<Plan, Long> {
	/** Lists plans from lowest to highest price. */
	List<Plan> findAllByOrderByPriceAsc();
}
