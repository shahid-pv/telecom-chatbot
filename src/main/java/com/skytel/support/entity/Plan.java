package com.skytel.support.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps a mobile plan from the plans catalog. */
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
