package com.skytel.support.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skytel.support.entity.Customer;
import com.skytel.support.repository.ActiveServiceRepository;
import com.skytel.support.repository.CustomerRepository;
import com.skytel.support.repository.PlanRepository;
import com.skytel.support.repository.TransactionRepository;

/** Provides read-only account details that the chat assistant can look up. */
@Component
@Transactional(readOnly = true)
public class AccountTools {
	
	private static final Logger log = LoggerFactory.getLogger(AccountTools.class);
	
	private final CustomerRepository customers;
	private final PlanRepository plans;
	private final TransactionRepository transactions;
	private final ActiveServiceRepository activeServiceRepository;

	public AccountTools(CustomerRepository customerRepository, PlanRepository planRepository,
			TransactionRepository transactionRepository, ActiveServiceRepository activeServiceRepository) {
		this.customers = customerRepository;
		this.plans = planRepository;
		this.transactions = transactionRepository;
		this.activeServiceRepository = activeServiceRepository;
	}

	/** Finds the customer attached to the current chat request. */
	private Optional<Customer> current(ToolContext context) {
		Object customerId = context == null ? null : context.getContext().get("customerId");
		return customerId instanceof Long customerKey ? customers.findByIdReadOnly(customerKey) : Optional.empty();
	}

	/** Returns the signed-in customer's prepaid balance. */
	@Tool(description = "Read the logged-in customer's current prepaid balance. Never use an id from the user.")
	public String getBalance(ToolContext context) {
		log.debug("Fetching balance for user");
		return current(context).map(customer -> "Account holder name: " + customer.getName() + ",Balance: ₹"
				+ customer.getBalance())
				.orElse("Not logged in or customer unavailable.");
	}

	/** Returns how much mobile data the signed-in customer has used. */
	@Tool(description = "Read mobile data used in MB for the logged-in customer.")
	public String getDataUsage(ToolContext context) {
		log.debug("Fetching data usage for user");
		return current(context)
				.map(customer -> "Account holder name: " + customer.getName() + ", Data used for today: "
						+ customer.getDataUsedDailyMb() + " MB, Data used for last 30 days: "
						+ customer.getDataUsedMb() + " MB")
				.orElse("Not logged in or customer unavailable.");
	}

	/** Lists the customer's transactions within the requested date range. */
	@Tool(description = "List transactions for the logged-in customer between ISO dates yyyy-MM-dd (e.g., 2026-12-31). The current year is 2026. Always calculate exact yyyy-MM-dd strings accurately")
	public String getTransactions(String fromDate, String toDate, ToolContext context) {
		log.debug("Fetching transactions for user between dates {} and {}", fromDate, toDate);
		var customer = current(context);
		if (customer.isEmpty()) {
			return "Not logged in.";
		}
		try {
			var start = LocalDate.parse(fromDate).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
			var end = LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
			var customerTransactions = transactions.byDate(customer.get().getId(), start, end);
			String transactionDetails = customerTransactions.isEmpty() ? "No transactions found."
					: customerTransactions.stream()
							.map(transaction -> transaction.getCreatedAt() + " " + transaction.getType() + " ₹"
									+ transaction.getAmount() + " — " + transaction.getDescription())
							.toList().toString();
			System.out.println(transactionDetails);
			return transactionDetails;
		} catch (Exception exception) {
			return "Date range unavailable.";
		}
	}

	/** Returns the signed-in customer's current plan and expiry date. */
	@Tool(description = "Read the current plan and expiry date of the logged-in customer.")
	public String getActivePlan(ToolContext context) {
		log.debug("Fetching active plan for user");
		return current(context)
				.map(customer -> plans.findById(customer.getPlanId())
						.map(plan -> "Account holder name: " + customer.getName() + ", Active plan : " + plan.getName()
								+ ", ₹" + plan.getPrice() + ", expires " + customer.getPlanExpiry())
						.orElse("No plan data."))
				.orElse("Not logged in.");
	}

	/** Lists the customer's active read-only services. */
	@Tool(description = "List active read-only services for the logged-in customer.")
	public String getActiveServices(ToolContext context) {
		log.debug("Fetching active services for user");
		var customer = current(context);
		return customer.map(account -> {
			var activeServices = activeServiceRepository.activeFor(account.getId());
			var serviceDetails = activeServices.isEmpty() ? "No active services."
					: activeServices.stream().map(service -> service.getName() + " ₹" + service.getPrice()).toList()
							.toString();
			return serviceDetails;
		}).orElse("Not logged in.");
	}

	/** Suggests catalog plans based on the customer's recorded data usage. */
	@Tool(description = "Suggest plans from the plans catalog based on the logged-in customer's usage. This does not change their plan.")
	public String recommendPlans(ToolContext context) {
		log.debug("Fetching recommended plans for user");
		return current(context)
				.map(customer -> "Customer usage " + customer.getDataUsedMb() + "MB. Available plans: "
						+ plans.findAllByOrderByPriceAsc().stream()
								.map(plan -> plan.getName() + " ₹" + plan.getPrice() + " " + plan.getDataGb() + "GB").toList())
				.orElse("Not logged in.");
	}
}
