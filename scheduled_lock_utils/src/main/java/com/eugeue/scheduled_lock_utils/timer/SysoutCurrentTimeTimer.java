package com.eugeue.scheduled_lock_utils.timer;

import com.eugeue.scheduled_lock_utils.annotation.ScheduledCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SysoutCurrentTimeTimer {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${server.timerName}")
    private String timeName;

    private static final SimpleDateFormat PATTERN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Scheduled(cron="0/3 * * * * ? ")
    @ScheduledCluster
    public void timer() {
        System.out.println("打印当前时间" + timeName + PATTERN.format(new Date()));
    }


}
