package com.skytel.support;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.test.util.ReflectionTestUtils;

import com.skytel.support.entity.Customer;
import com.skytel.support.repository.ActiveServiceRepository;
import com.skytel.support.repository.CustomerRepository;
import com.skytel.support.repository.PlanRepository;
import com.skytel.support.repository.TransactionRepository;
import com.skytel.support.service.AccountTools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReadOnlyToolsTest {
    @Test
    void guestIsRefusedByAccountTools() {
        var customers = mock(CustomerRepository.class);
        var tools = new AccountTools(customers, mock(PlanRepository.class), mock(TransactionRepository.class), mock(ActiveServiceRepository.class));
        assertEquals("Not logged in or customer unavailable.", tools.getBalance(new ToolContext(Map.of())));
        verifyNoInteractions(customers);
    }

    @Test
    void customerIdIsNeverAnLlmVisibleToolArgument() {
        for (var method : AccountTools.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                assertTrue(Arrays.stream(method.getParameterTypes()).noneMatch(Long.class::equals));
            }
        }
    }

    @Test
    void balanceUsesOnlyCustomerIdFromToolContext() throws Exception {
        var customers = mock(CustomerRepository.class);
        var constructor = Customer.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var customer = constructor.newInstance();
        ReflectionTestUtils.setField(customer, "name", "Asha");
        ReflectionTestUtils.setField(customer, "balance", new BigDecimal("87.00"));
        when(customers.findByIdReadOnly(1L)).thenReturn(Optional.of(customer));
        var tools = new AccountTools(customers, mock(PlanRepository.class), mock(TransactionRepository.class), mock(ActiveServiceRepository.class));
        assertEquals("Account holder name: Asha,Balance: ₹87.00",
                tools.getBalance(new ToolContext(Map.of("customerId", 1L))));
        verify(customers).findByIdReadOnly(1L);
    }
}
