package com.eugeue.scheduled_lock_utils.timer;

import com.eugeue.scheduled_lock_utils.annotation.ScheduledCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class SysoutTimer {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${server.timerName}")
    private String timeName;

    private static final SimpleDateFormat PATTERN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron="0/5 * * * * ? ")
    @ScheduledCluster
    public void timer() {
        for (int i = 0; i < 3; i ++) {
            System.out.println("=====实现打印业务=====" + timeName + PATTERN.format(new Date()));
        }
    }

}
