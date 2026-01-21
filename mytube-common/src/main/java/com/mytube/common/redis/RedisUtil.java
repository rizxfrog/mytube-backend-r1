package com.mytube.common.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final long REDIS_DEFAULT_EXPIRE_TIME = 60 * 60;
    public static final TimeUnit REDIS_DEFAULT_EXPIRE_TIMEUNIT = TimeUnit.SECONDS;

    @Data
//    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZObjTime {
        private Object member;
        private Date time;

        public ZObjTime(Object member, Date time) {
            this.member = member;
            this.time = time;
        }

        public Object getMember() {
            return member;
        }

        public Date getTime() {
            return time;
        }
    }

    @Data
//    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZObjScore {
        private Object member;
        private Double score;

        public ZObjScore(Object member, Double score) {
            this.member = member;
            this.score = score;
        }

        public Object getMember() {
            return member;
        }

        public Double getScore() {
            return score;
        }
    }

    public Long getExpire(String key) { return redisTemplate.getExpire(key, TimeUnit.SECONDS); }
    public void setExpire(String key, long time) { redisTemplate.expire(key, time, TimeUnit.SECONDS); }
    public void removeCache(String key){ redisTemplate.delete(key); }
    public void deleteKeysWithPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) { redisTemplate.delete(keys); }
    }
    public boolean isExist(String redisKey) { return redisTemplate.hasKey(redisKey); }

    public Set<Object> zgetAllMembers(String key, long expireSec) {
        long now = System.currentTimeMillis();
        long tts = now - expireSec * 1000;
        return redisTemplate.opsForZSet().rangeByScore(key, tts+1, Long.MAX_VALUE);
    }
    public Set<Object> zgetMembersWithLimit(String key, long expireSec, long offset, long count) {
        long now = System.currentTimeMillis();
        long tts = now - expireSec * 1000;
        return redisTemplate.opsForZSet().rangeByScore(key, tts+1, Long.MAX_VALUE, offset, count);
    }
    public Set<Object> zRange(String key, long start, long stop) { return redisTemplate.opsForZSet().range(key, start, stop); }
    public Set<Object> zReverange(String key, long start, long stop) { return redisTemplate.opsForZSet().reverseRange(key, start, stop); }
    public List<ZObjScore> zReverangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZObjScore> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) { list.add(new ZObjScore(tuple.getValue(), tuple.getScore())); }
        return list;
    }
    public List<ZObjTime> zReverangeWithTime(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZObjTime> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) { list.add(new ZObjTime(tuple.getValue(), new Date(tuple.getScore().longValue()))); }
        return list;
    }
    public Long reverseRank(String key, Object member) {
        Long longValue = redisTemplate.opsForZSet().reverseRank(key, member);
        if (null != longValue) { return longValue; }
        return redisTemplate.opsForZSet().size(key);
    }
    public boolean zset(String key, Object object){ long now = System.currentTimeMillis(); return this.zsetWithScore(key, object, now); }
    public boolean zsetWithScore(String key, Object object, double score){ return redisTemplate.opsForZSet().add(key, object, score); }
    public Long zsetOfCollectionByTime(String key, Collection<ZObjTime> zObjTimes) { return redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(zObjTimes)); }
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByTime(Collection<ZObjTime> zObjTimes) {
        return zObjTimes.stream().map(zObjTime -> (ZSetOperations.TypedTuple<Object>) new DefaultTypedTuple<>(zObjTime.getMember(), (double) zObjTime.getTime().getTime())).collect(Collectors.toSet());
    }
    public Long zsetOfCollectionByScore(String key, Collection<ZObjScore> zObjScores) { return redisTemplate.opsForZSet().add(key, convertToTupleSetByScore(zObjScores)); }
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByScore(Collection<ZObjScore> zObjScores) {
        return zObjScores.stream().map(zObjScore -> (ZSetOperations.TypedTuple<Object>) new DefaultTypedTuple<>(zObjScore.getMember(), zObjScore.getScore())).collect(Collectors.toSet());
    }
    public long zCount(String key, long min, long max){ return redisTemplate.opsForZSet().count(key, min, max); }
    public Long zCard(String key) { return redisTemplate.opsForZSet().zCard(key); }
    public void zsetDelMember(String key, Object value) { redisTemplate.opsForZSet().remove(key, value); }
    public Double zscore(String key, Object value) { return redisTemplate.opsForZSet().score(key, value); }
    public Double zincrby(String key, Object value, double score) { return redisTemplate.opsForZSet().incrementScore(key, value, score); }
    public Boolean zsetExist(String key, Object value) { Double d = zscore(key, value); return null != d; }
    public Boolean zsetByLimit(String key, Object value, Integer limit) {
        Boolean result = this.zset(key, value);
        Long count = this.zCard(key);
        if (count != null && count > limit) { redisTemplate.opsForZSet().removeRange(key, 0, count-limit-1); }
        return result;
    }

    public Set<Object> getMembers(String key) { return redisTemplate.opsForSet().members(key); }
    public Boolean isMember(String key, Object value) { return redisTemplate.opsForSet().isMember(key, value); }
    public void addMember(String key, Object value) { redisTemplate.opsForSet().add(key, value); }
    public void addMembers(String key, List<Object> list) { redisTemplate.opsForSet().add(key, list.toArray()); }
    public void addExMember(String key, String value, Integer time) { redisTemplate.opsForSet().add(key, value); setExpire(key, time); }
    public void delMember(String key, Object value) { redisTemplate.opsForSet().remove(key, value); }
    public Long scard(String key) { return redisTemplate.opsForSet().size(key); }
    public Set<Object> srandmember(String key, Integer count) { return redisTemplate.opsForSet().distinctRandomMembers(key, count); }

    public void setValue(String key, Object value) { redisTemplate.opsForValue().set(key, value); }
    public void setObjectValue(String key, Object value) { String jsonString = JSON.toJSONString(value); setValue(key, jsonString); }
    public void setExValue(String key, Object value) { setExValue(key, value, REDIS_DEFAULT_EXPIRE_TIME, REDIS_DEFAULT_EXPIRE_TIMEUNIT); }
    public void setExValue(String key, Object value, long time) { setExValue(key, value, time, REDIS_DEFAULT_EXPIRE_TIMEUNIT); }
    public void setExValue(String key, Object value, long time, TimeUnit timeUnit) { redisTemplate.opsForValue().set(key, value, time, timeUnit); }
    public void setExObjectValue(String key, Object value) { String jsonString = JSON.toJSONString(value); setExValue(key, jsonString); }
    public void setExObjectValue(String key, Object value, long time, TimeUnit timeUnit) { String jsonString = JSON.toJSONString(value); setExValue(key, jsonString, time, timeUnit); }
    public Object getValue(Object key) { return redisTemplate.opsForValue().get(key); }
    public String getObjectString(String key) { return (String) redisTemplate.opsForValue().get(key); }
    public <T> T getObject(String key, Class<T> clazz) { String objectString = (String) redisTemplate.opsForValue().get(key); if (StringUtils.isNotBlank(objectString)) { return JSONObject.parseObject(objectString, clazz); } return null; }
    public void incr(String key) { redisTemplate.opsForValue().increment(key, 1); }
    public void decr(String key) { redisTemplate.opsForValue().decrement(key, 1); }
    public void delValue(String key) { redisTemplate.opsForValue().getOperations().delete(key); }
    public void delValues(Collection<String> keys) { redisTemplate.opsForValue().getOperations().delete(keys); }

    public Long setAllList(String key, List list) {
        List<String> dataList = new ArrayList<>();
        for (Object temp : list) { dataList.add(JSON.toJSONString(temp)); }
        return this.redisTemplate.opsForList().rightPushAll(key, dataList);
    }
    public <T> List<T> getAllList(String key, Class<T> clazz) {
        List list = this.redisTemplate.opsForList().range(key, 0, -1);
        List<T> resultList = new ArrayList<>();
        for (Object temp : list) { resultList.add(JSON.parseObject((String) temp, clazz)); }
        return resultList;
    }

    public void setExValueForToday(String key, Object value) {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long remainTime = ChronoUnit.SECONDS.between(LocalDateTime.now(),midnight);
        redisTemplate.opsForValue().set(key, value, remainTime, TimeUnit.SECONDS);
    }
    public void setExValueForWeekend(String key, Object value) {
        LocalDateTime midnight = LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long remainTime = ChronoUnit.SECONDS.between(LocalDateTime.now(),midnight);
        redisTemplate.opsForValue().set(key, value, remainTime, TimeUnit.SECONDS);
    }
}

