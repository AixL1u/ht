package com.heritage.aspect;

import com.heritage.annotation.Log;
import com.heritage.domain.OperationLog;
import com.heritage.repo.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogRepository logRepository;

    @Pointcut("@annotation(com.heritage.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        long time = System.currentTimeMillis() - beginTime;

        saveLog(point, time);
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        OperationLog sysLog = new OperationLog();
        Log log = method.getAnnotation(Log.class);
        if (log != null) {
            sysLog.setOperation(log.value());
        }

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");

        Object[] args = joinPoint.getArgs();
        try {
            String params = Arrays.toString(args);
            if (params.length() > 500) params = params.substring(0, 500) + "...";
            sysLog.setParams(params);
        } catch (Exception e) {
            // ignore param serialization errors
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        sysLog.setIp(request.getRemoteAddr());

        String username = "";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        sysLog.setUsername(username);

        sysLog.setExecutionTime(time);
        sysLog.setCreatedAt(Instant.now());

        logRepository.save(sysLog);
    }
}
