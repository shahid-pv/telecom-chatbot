package com.skytel.support.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps an add-on or other service attached to a customer account. */
@Entity
@Table(name = "active_services")
public class ActiveService {
	@Id
	private Long id;
	@Column(name = "customer_id")
	private Long customerId;
	private String name;
	private BigDecimal price;
	private String status;
	@Column(name = "started_at")
	private OffsetDateTime startedAt;

	protected ActiveService() {
	}

	public Long getId() {
		return id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getStatus() {
		return status;
	}

	public OffsetDateTime getStartedAt() {
		return startedAt;
	}
}
