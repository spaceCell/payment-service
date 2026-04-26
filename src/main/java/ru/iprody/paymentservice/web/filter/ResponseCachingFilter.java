package ru.iprody.paymentservice.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class ResponseCachingFilter implements Filter {

    public static final String WRAPPED_RESPONSE_ATTRIBUTE = "wrappedResponse";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest
                && response instanceof HttpServletResponse httpResponse) {
            var method = HttpMethod.valueOf(httpRequest.getMethod());
            if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH)) {
                var wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
                httpRequest.setAttribute(WRAPPED_RESPONSE_ATTRIBUTE, wrappedResponse);
                try {
                    chain.doFilter(httpRequest, wrappedResponse);
                } finally {
                    wrappedResponse.copyBodyToResponse();
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
