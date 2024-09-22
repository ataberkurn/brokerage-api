package com.brokerage.api.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
public class IpRateLimitingFilter implements Filter {

    private CacheManager ehcacheCacheManager;

    public IpRateLimitingFilter(@Qualifier("ehcacheCacheManager") CacheManager ehcacheCacheManager) {
        this.ehcacheCacheManager = ehcacheCacheManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = httpRequest.getRemoteAddr();

        Cache cache = ehcacheCacheManager.getCache("buckets");
        Bucket bucket = resolveBucket(ip, cache);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }

    private Bucket resolveBucket(String ip, Cache cache) {
        return Optional.ofNullable(cache.get(ip, Bucket.class))
                .orElseGet(() -> createNewBucket(ip, cache));
    }

    private Bucket createNewBucket(String ip, Cache cache) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        Bucket bucket = Bucket.builder().addLimit(limit).build();
        cache.put(ip, bucket);
        return bucket;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
