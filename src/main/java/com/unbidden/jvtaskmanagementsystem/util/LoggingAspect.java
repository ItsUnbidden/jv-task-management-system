package com.unbidden.jvtaskmanagementsystem.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final Map<Class<?>, Logger> loggers = new HashMap<>();

    @Pointcut("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..))")
    public void serviceMethodAdvice() {

    }

    @Before("serviceMethodAdvice()")
    public void beforeServiceMethodAdvice(JoinPoint joinPoint) {
        StringBuilder builder = new StringBuilder();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] params = method.getParameters();
        Object[] args = joinPoint.getArgs();
        Logger logger = getLogger(signature.getDeclaringType());

        for (int i = 0; i < args.length; i++) {
            builder.append(params[i].getName()).append(" - ");
            
            if (params[i].isAnnotationPresent(DisableLogging.class)) {
                builder.append("SECRET");
            } else if (args[i] == null) {
                builder.append("NULL");
            } else {
                builder.append(args[i].toString());
            }
            builder.append("; ");
        }
        builder.delete(builder.length() - 2, builder.length());

        logger.debug("Service method \"" + method.getName() + "\" was triggered. Args: "
                + builder.toString());
    }

    @After("serviceMethodAdvice()")
    public void afterServiceMethodAdvice(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Logger logger = getLogger(signature.getDeclaringType());
        
        logger.debug("Service method \"" + signature.getName() + "\" has finished execution.");
    }

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.security.project."
            + "provider..*getProject(..))")
    public void beforeGetProject(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Logger logger = getLogger(signature.getDeclaringType());

        logger.debug("Project provider " + signature.getDeclaringType().getSimpleName()
                + " is trying to load project using provider's entity id " + joinPoint.getArgs()[0]);
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
