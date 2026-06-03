package dev.mooka.translationnexus.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_HEADER = "X-Trace-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            String traceId = httpServletRequest.getHeader(TRACE_HEADER);
            if (traceId == null || traceId.trim().isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put(TRACE_ID_KEY, traceId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
