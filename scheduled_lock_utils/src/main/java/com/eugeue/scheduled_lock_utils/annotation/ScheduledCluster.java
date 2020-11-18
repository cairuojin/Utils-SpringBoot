package com.eugeue.scheduled_lock_utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在定时器方法上添加此自定义注解以添加redis分布式锁，用以解决多台服务器启动时只启动一次定时器的场景
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
    public @interface ScheduledCluster {
}
