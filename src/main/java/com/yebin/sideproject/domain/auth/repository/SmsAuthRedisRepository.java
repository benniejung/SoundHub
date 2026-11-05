package com.yebin.sideproject.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SmsAuthRedisRepository {

    private static final String CODE_PREFIX = "sms:code:";
    private static final String ATTEMPT_PREFIX = "sms:attempt:";
    private static final String COOLDOWN_PREFIX = "sms:cooldown:";
    private static final String DAILY_PREFIX = "sms:daily:";
    private static final String IP_PREFIX = "sms:ip:hour:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(10);
    private static final Duration ONE_DAY = Duration.ofDays(1);
    private static final Duration ONE_HOUR = Duration.ofHours(1);
    private static final long MAX_ATTEMPTS = 5;
    private static final long DAILY_LIMIT = 10;
    private static final long IP_HOURLY_LIMIT = 20;

    // INCR과 만료 설정을 한 번에 실행해, 만료 시간 없는 키가 남지 않도록 한다
    private static final RedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    // IP당 1시간 20회
    public boolean isWithinIpLimit(String ip) {
        return increment(IP_PREFIX + ip, ONE_HOUR) <= IP_HOURLY_LIMIT;
    }

    // 재발송 간격(10초)
    public boolean tryAcquireCooldown(String phoneNumber) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(COOLDOWN_PREFIX + phoneNumber, "1", COOLDOWN_TTL));
    }

    // 번호당 하루 10회
    public boolean isWithinDailyLimit(String phoneNumber) {
        return increment(DAILY_PREFIX + phoneNumber, ONE_DAY) <= DAILY_LIMIT;
    }

    // 새 코드를 저장하면 이전 코드의 실패 횟수는 초기화한다
    public void saveCode(String phoneNumber, String code) {
        redisTemplate.opsForValue().set(CODE_PREFIX + phoneNumber, code, CODE_TTL);
        redisTemplate.delete(ATTEMPT_PREFIX + phoneNumber);
    }

    public Optional<String> findCode(String phoneNumber) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CODE_PREFIX + phoneNumber));
    }

    // 실패 횟수를 올리고, 5회가 되면 코드를 폐기한 뒤 true를 반환한다
    public boolean recordFailedAttempt(String phoneNumber) {
        if (increment(ATTEMPT_PREFIX + phoneNumber, CODE_TTL) < MAX_ATTEMPTS) {
            return false;
        }
        deleteCode(phoneNumber);
        return true;
    }

    public void deleteCode(String phoneNumber) {
        redisTemplate.delete(List.of(CODE_PREFIX + phoneNumber, ATTEMPT_PREFIX + phoneNumber));
    }

    private long increment(String key, Duration ttl) {
        return redisTemplate.execute(INCREMENT_SCRIPT, List.of(key), String.valueOf(ttl.toMillis()));
    }
}
