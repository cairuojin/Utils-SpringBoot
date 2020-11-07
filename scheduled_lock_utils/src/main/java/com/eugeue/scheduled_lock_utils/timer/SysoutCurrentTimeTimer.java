package com.eugeue.scheduled_lock_utils.timer;

import com.eugeue.scheduled_lock_utils.annotation.ScheduledCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SysoutCurrentTimeTimer {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${server.timerName}")
    private String timeName;

    @Scheduled(cron="0/1 * * * * ? ")
    @ScheduledCluster(expire = 800)
    public void timer() {
        System.out.println("打印当前时间" + timeName + System.currentTimeMillis());
    }


}
