package com.brokerage.api.aspect;

import com.brokerage.api.entity.User;
import com.brokerage.api.enumeration.Role;
import com.brokerage.api.exception.UnauthorizedAccessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class AccessCheckAspect {

    @Around("@annotation(com.brokerage.api.annotation.CheckCustomerAccess)")
    public Object checkAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID targetCustomerId = (UUID) joinPoint.getArgs()[0];

        if (currentUser.getRole() == Role.EMPLOYEE || currentUser.getId().equals(targetCustomerId)) {
            return joinPoint.proceed();
        } else {
            throw new UnauthorizedAccessException("you do not have permission to access this data");
        }
    }
}
