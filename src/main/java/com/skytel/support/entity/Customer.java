package com.skytel.support.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Maps the customer account fields stored in the database. */
@Entity
@Table(name = "customers")
public class Customer {
	@Id
	private Long id;
	private String name;
	private String phone;
	private String email;
	@Column(name = "plan_id")
	private Long planId;
	private BigDecimal balance;
	@Column(name = "plan_expiry")
	private LocalDate planExpiry;
	@Column(name = "data_used_mb")
	private Integer dataUsedMb;
	private String status;
	@Column(name = "data_used_daily_mb")
	private Integer dataUsedDailyMb;

	protected Customer() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public Long getPlanId() {
		return planId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public LocalDate getPlanExpiry() {
		return planExpiry;
	}

	public Integer getDataUsedMb() {
		return dataUsedMb;
	}

	public String getStatus() {
		return status;
	}
	public Integer getDataUsedDailyMb() {
		return dataUsedDailyMb;
	}
	
}
