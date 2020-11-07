package com.eugeue.scheduled_lock_utils.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.SourceLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Aspect
public class ScheduledAOP {

    @Value("${server.timerName}")
    private String timeName;

    public static final int LOCK_EXPIRE = 6000; // ms


    @Autowired
    private RedisTemplate redisTemplate;

    public boolean lock(String lock) {
        return (boolean) redisTemplate.execute((RedisCallback) connection -> {
            //获取时间毫秒值
            long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
            //获取锁
            Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());
            if (acquire) {
                return true;
            } else {
                byte[] bytes = connection.get(lock.getBytes());
                //非空判断
                if (Objects.nonNull(bytes) && bytes.length > 0) {
                    long expireTime = Long.parseLong(new String(bytes));
                    // 如果锁已经过期
                    if (expireTime < System.currentTimeMillis()) {
                        // 重新加锁，防止死锁
                        byte[] set = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                        return Long.parseLong(new String(set)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
    }



    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object doInvoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String lockKey = signature.getDeclaringTypeName() + "." + signature.getName();
        if (lock(lockKey)) {
            try {
                System.out.println("线程（" + Thread.currentThread().getName() + "）获取到锁，开始执行操作");
                return joinPoint.proceed();
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
