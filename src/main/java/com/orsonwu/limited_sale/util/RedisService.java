package com.orsonwu.limited_sale.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private JedisPool jedisPool;

    /**
     * Set a value in redis
     *
     * @param key
     * @param value
     */
    public void setValue(String key, Long value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value.toString());
        jedisClient.close();
    }

    /**
     * set string value
     *
     * @param key
     * @param value
     */
    public void setValue(String key, String value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value);
        jedisClient.close();
    }
    /**
     * set string value in hash
     *
     * @param field
     * @param value
     */
    public void setValueInHash(String hashKey, String field, String value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.hset(hashKey, field, value);
        jedisClient.close();
    }

    /**
     * Return 'nil' if the value can't be found
     * @param hashKey
     * @param field
     * @return
     */
    public String getValueInHash(String hashKey, String field) {
        Jedis jedisClient = jedisPool.getResource();
        String retval = jedisClient.hget(hashKey, field);
        jedisClient.close();
        return retval;
    }

    public List<String> getAllValuesInHash(String hashKey){
        Jedis jedisClient = jedisPool.getResource();
        List<String> retval = jedisClient.hvals(hashKey);
        jedisClient.close();
        return retval;
    }

    /**
     * get a value from redis
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        Jedis jedisClient = jedisPool.getResource();
        String value = jedisClient.get(key);

        jedisClient.close();
        return value;
    }

    /**
     * dcre a value in redis with atomic operations
     * @param key
     * @return
     * @throws Exception
     */
    public boolean stockDeductValidator(String key)  {
        try(Jedis jedisClient = jedisPool.getResource()) {

            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "                 local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "                 if( stock <=0 ) then\n" +
                    "                    return -1\n" +
                    "                 end;\n" +
                    "                 redis.call('decr',KEYS[1]);\n" +
                    "                 return stock - 1;\n" +
                    "             end;\n" +
                    "             return -1;";

            Long stock = (Long) jedisClient.eval(script, Collections.singletonList(key), Collections.emptyList());
            if (stock < 0) {
                System.out.println("Sorry, sold out!");
                return false;
            } else {
                System.out.println("Congratulation，bought the item!");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("Stock decre failed：" + throwable.toString());
            return false;
        }
    }
    /**
    * Add sale event limit to user
    */
    public void addLimitMember(long eventId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.sadd("limitedSale_restrainedUsers:" + eventId, String.valueOf(userId));
        jedisClient.close();
    }

    /**
     * Check if the user is restricted from this sale event
     *
     * @param eventId
     * @param userId
     * @return
     */
    public boolean isInLimitMember(long eventId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        boolean sismember = jedisClient.sismember("limitedSale_restrainedUsers:" + eventId, String.valueOf(userId));
        jedisClient.close();
        log.info("userId:{}  eventId:{}  if restrained:{}", userId, eventId, sismember);
        return sismember;
    }

    /**
     * Remove restriction to a user
     *
     * @param eventId
     * @param userId
     */
    public void removeLimitMember(long eventId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.srem("limitedSale_restrainedUsers:" + eventId, String.valueOf(userId));
        jedisClient.close();
    }

    /**
     * revert the stock deduction when the order exceeded the time limit
     *
     * @param key
     */
    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }

    /**
     * Get distributed lock from redis
     *
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     */
    public boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        Jedis jedisClient = jedisPool.getResource();
        //nx means key exists do nothing
        //px means the lock has an expiring time
        String result = jedisClient.set(lockKey, requestId, "NX", "PX", expireTime);
        jedisClient.close();
        if ("OK".equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * release distributed lock from redis
     *
     * @param lockKey
     * @param requestId unique id for each thread
     * @return if relaese successfully
     */
    public boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedisClient = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                        "then return redis.call('del', KEYS[1]) " +
                        "else return 0 end";
        Long result = (Long) jedisClient.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        jedisClient.close();
        if (result == 1L) {
            return true;
        }
        return false;
    }
}

