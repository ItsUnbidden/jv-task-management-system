package com.unbidden.jvtaskmanagementsystem.util;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private Map<Class<?>, Logger> loggers = new HashMap<>();

    @Pointcut("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..))")
    public void serviceMethodAdvice() {

    }

    @Before("serviceMethodAdvice()")
    public void beforeServiceMethodAdvice(JoinPoint joinPoint) {
        StringBuilder builder = new StringBuilder();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Logger logger = getLogger(signature.getDeclaringType());
        
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            builder.append(signature.getParameterNames()[i]).append(" - ")
                    .append(joinPoint.getArgs()[i].toString()).append("; ");
        }
        builder.delete(builder.length() - 2, builder.length());

        logger.info("Service method \"" + signature.getName() + "\" was triggered. Args: "
                + builder.toString());
    }

    @After("serviceMethodAdvice()")
    public void afterServiceMethodAdvice(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Logger logger = getLogger(signature.getDeclaringType());
        
        logger.info("Service method \"" + signature.getName() + "\" has finished execution.");
    }

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.security.project."
            + "provider..*getProject(..))")
    public void beforeGetProject(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Logger logger = getLogger(signature.getDeclaringType());

        logger.info("Project provider " + signature.getDeclaringType().getSimpleName()
                + "is trying to load project " + joinPoint.getArgs()[0]);
    }

    private Logger getLogger(Class<?> clazz) {
        Logger logger = loggers.get(clazz);
        if (logger == null) {
            logger = LogManager.getLogger(clazz);
            loggers.put(clazz, logger);
        }
        return logger;
    }
}
