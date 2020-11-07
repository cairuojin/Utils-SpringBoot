package com.eugeue.scheduled_lock_utils.timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class SysoutTimer {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${server.timerName}")
    private String timeName;

    @Scheduled(cron="0/5 * * * * ? ")
    public void timer() {
        for (int i = 0; i < 3; i ++) {
            System.out.println("实现打印业务" + timeName);
        }
    }

}
