package org.example.centralserver.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Save an object in Redis
    public void saveObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // Retrieve an object from Redis
    public <T> T getObject(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}
