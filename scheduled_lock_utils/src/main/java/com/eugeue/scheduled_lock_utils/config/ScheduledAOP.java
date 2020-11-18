package com.eugeue.scheduled_lock_utils.config;

import com.eugeue.scheduled_lock_utils.annotation.ScheduledCluster;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Component
@Aspect
public class ScheduledAOP {

    @Autowired
    private RedisTemplate redisTemplate;

//    private static final int expire = 1000 * 60 * 30;   //30分钟
    private static final int expire = 1000;   //1秒

    private static final SimpleDateFormat PATTERN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 尝试上锁
     * @param lock
     * @return
     */
    public boolean lock(String lock) {
        return (boolean) redisTemplate.execute((RedisCallback) connection -> {
            //获取锁
            Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(System.currentTimeMillis() + expire).getBytes());
            if (acquire) {
                return true;
            } else {
                byte[] bytes = connection.get(lock.getBytes());
                //非空判断
                if (Objects.nonNull(bytes) && bytes.length > 0) {
                    long expireTime = Long.parseLong(new String(bytes));
                    // 如果锁已经过期
                    if (expireTime < System.currentTimeMillis()) {
                        // 重新加锁
                        byte[] set = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + expire).getBytes());
                        //由于此处非原子性操作，需加多一次判断
                        return Long.parseLong(new String(set)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
    }


    /**
     * AOP方法
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled) " +
            "&& @annotation(com.eugeue.scheduled_lock_utils.annotation.ScheduledCluster)")
    public Object doInvoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String lockKey = signature.getDeclaringTypeName() + "." + signature.getName() + "." + PATTERN.format(new Date());
        if (lock(lockKey)) {
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
