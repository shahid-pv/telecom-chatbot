package com.skytel.support.service;

import com.skytel.support.entity.*;
import com.skytel.support.repository.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Provides read-only account details that the chat assistant can look up. */
@Component
@Transactional(readOnly = true)
public class AccountTools {
	private final CustomerRepository customers;
	private final PlanRepository plans;
	private final TransactionRepository transactions;
	private final ActiveServiceRepository services;

	public AccountTools(CustomerRepository c, PlanRepository p, TransactionRepository t, ActiveServiceRepository s) {
		customers = c;
		plans = p;
		transactions = t;
		services = s;
	}

	/** Finds the customer attached to the current chat request. */
	private Optional<Customer> current(ToolContext c) {
		Object id = c == null ? null : c.getContext().get("customerId");
		return id instanceof Long n ? customers.findByIdReadOnly(n) : Optional.empty();
	}

	@Tool(description = "Read the logged-in customer's current prepaid balance. Never use an id from the user.")
	/** Returns the signed-in customer's prepaid balance. */
	public String getBalance(ToolContext c) {
		return current(c).map(x -> "Balance: ₹" + x.getBalance()).orElse("Not logged in or customer unavailable.");
	}

	@Tool(description = "Read mobile data used in MB for the logged-in customer.")
	/** Returns how much mobile data the signed-in customer has used. */
	public String getDataUsage(ToolContext c) {
		return current(c).map(x -> "Data used: " + x.getDataUsedMb() + " MB")
				.orElse("Not logged in or customer unavailable.");
	}

	@Tool(description = "List transactions for the logged-in customer between ISO dates yyyy-MM-dd. Use today when the user says yesterday.")
	/** Lists the customer's transactions within the requested date range. */
	public String getTransactions(String fromDate, String toDate, ToolContext c) {
		var u = current(c);
		if (u.isEmpty())
			return "Not logged in.";
		try {
			var a = LocalDate.parse(fromDate).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
			var b = LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
			var rows = transactions.byDate(u.get().getId(), a, b);
			return rows.isEmpty() ? "No transactions found."
					: rows.stream().map(t -> t.getCreatedAt() + " " + t.getType() + " ₹" + t.getAmount() + " — "
							+ t.getDescription()).toList().toString();
		} catch (Exception e) {
			return "Date range unavailable.";
		}
	}

	@Tool(description = "Read the current plan and expiry date of the logged-in customer.")
	/** Returns the signed-in customer's current plan and expiry date. */
	public String getActivePlan(ToolContext c) {
		System.out.println("getActivePlan");
		return current(c).map(u -> plans.findById(u
				.getPlanId())
				.map(p -> "Account holder name: "+u.getName()+ ", Active plan : "+ p.getName() + ", ₹" + p.getPrice() + ", expires " + u.getPlanExpiry())
				//.map(p -> "No plan data.")
				.orElse("No plan data.")).orElse("Not logged in.");
	}

	@Tool(description = "List active read-only services for the logged-in customer.")
	/** Lists the customer's active read-only services. */
	public String getActiveServices(ToolContext c) {
		var u = current(c);
		return u.map(x -> {
			var r = services.activeFor(x.getId());
			return r.isEmpty() ? "No active services."
					: r.stream().map(s -> s.getName() + " ₹" + s.getPrice()).toList().toString();
		}).orElse("Not logged in.");
	}

	@Tool(description = "Suggest plans from the plans catalog based on the logged-in customer's usage. This does not change their plan.")
	/** Suggests catalog plans based on the customer's recorded data usage. */
	public String recommendPlans(ToolContext c) {
		return current(c)
				.map(u -> "Customer usage " + u.getDataUsedMb() + "MB. Available plans: "
						+ plans.findAllByOrderByPriceAsc().stream()
								.map(p -> p.getName() + " ₹" + p.getPrice() + " " + p.getDataGb() + "GB").toList())
				.orElse("Not logged in.");
	}
}
