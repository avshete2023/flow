package com.flow.monitoring.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    @Test
    void shouldReuseIncomingCorrelationId() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-test-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-test-1");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String generated = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generated).isNotBlank();
        assertThat(UUID.fromString(generated)).isNotNull();
    }
}

