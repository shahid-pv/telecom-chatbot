package com.skytel.support.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
public class Plan {
	@Id
	private Long id;
	private String name;
	private String type;
	private BigDecimal price;
	@Column(name = "validity_days")
	private Integer validityDays;
	@Column(name = "data_gb")
	private BigDecimal dataGb;
	private String description;

	protected Plan() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Integer getValidityDays() {
		return validityDays;
	}

	public BigDecimal getDataGb() {
		return dataGb;
	}

	public String getDescription() {
		return description;
	}
}
