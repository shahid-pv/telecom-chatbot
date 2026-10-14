package com.skytel.support;

import com.skytel.support.repository.*;
import com.skytel.support.service.AccountTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Optional;
import com.skytel.support.entity.Customer;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadOnlyToolsTest {
    @Test void guestIsRefusedByAccountTools() {
        var customers = mock(CustomerRepository.class);
        var tools = new AccountTools(customers, mock(PlanRepository.class), mock(TransactionRepository.class), mock(ActiveServiceRepository.class));
        assertEquals("Not logged in or customer unavailable.", tools.getBalance(new ToolContext(Map.of())));
        verifyNoInteractions(customers);
    }

    @Test void customerIdIsNeverAnLlmVisibleToolArgument() {
        for (var method : AccountTools.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(org.springframework.ai.tool.annotation.Tool.class)) {
                assertTrue(java.util.Arrays.stream(method.getParameterTypes()).noneMatch(Long.class::equals));
            }
        }
    }

    @Test void balanceUsesOnlyCustomerIdFromToolContext() throws Exception {
        var customers = mock(CustomerRepository.class);
        var constructor = Customer.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var customer = constructor.newInstance();
        ReflectionTestUtils.setField(customer, "balance", new BigDecimal("87.00"));
        when(customers.findByIdReadOnly(1L)).thenReturn(Optional.of(customer));
        var tools = new AccountTools(customers, mock(PlanRepository.class), mock(TransactionRepository.class), mock(ActiveServiceRepository.class));
        assertEquals("Balance: ₹87.00", tools.getBalance(new ToolContext(Map.of("customerId", 1L))));
        verify(customers).findByIdReadOnly(1L);
    }
}
