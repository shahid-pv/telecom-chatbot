package com.skytel.support.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Maps a billing or recharge transaction for a customer account. */
@Entity
@Table(name = "transactions")
public class Transaction {
	@Id
	private Long id;
	@Column(name = "customer_id")
	private Long customerId;
	private String type;
	private BigDecimal amount;
	private String description;
	@Column(name = "created_at")
	private OffsetDateTime createdAt;

	protected Transaction() {
	}

	public Long getId() {
		return id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getDescription() {
		return description;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
