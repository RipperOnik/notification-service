package com.andreykan.notification.infrastructure.observability;

import com.andreykan.notification.application.logging.LogExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(logExecution)")
    public Object logExecution(
            ProceedingJoinPoint joinPoint,
            LogExecution logExecution
    ) throws Throwable {
        Class<?> targetClass =
                AopUtils.getTargetClass(joinPoint.getTarget());

        Logger log =
                LoggerFactory.getLogger(targetClass);

        String operation =
                logExecution.value().isBlank() ?
                        joinPoint.getSignature().getName() :
                        logExecution.value();

        long startedAt = System.nanoTime();

        log.debug("Starting operation={}", operation);

        try {
            Object result = joinPoint.proceed();

            long durationMs =
                    (System.nanoTime() - startedAt) / 1_000_100;

            log.info(
                    "Completed operation={} durationMs={}",
                    operation,
                    durationMs
            );

            return result;
        }

        catch (Throwable exception) {
            long durationMs =
                    (System.nanoTime() - startedAt)
                            / 1_000_000;

            log.error(
                    "Failed operation={} durationMs={} exception={}",
                    operation,
                    durationMs,
                    exception.getClass().getSimpleName(),
                    exception
            );

            throw exception;
        }
    }
}
