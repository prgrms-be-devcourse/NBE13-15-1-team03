package com.kdt.cafe.config;

import com.kdt.cafe.dto.menu.MenuResponseDTO;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

// 캐시 보관 규칙
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                GenericJacksonJsonRedisSerializer.builder().build() // JSON 변환기
                        )
                );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer availableMenusCache(JsonMapper jsonMapper) {
        // "List<MenuResponseDTO>" 라는 타입 정보를 만든다
        JavaType menuListType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, MenuResponseDTO.class);

        // 이 타입으로 저장·복원하는 변환기
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, menuListType)
                ));

        // "availableMenus" 캐시에만 이 설정을 적용
        return builder -> builder.withCacheConfiguration("availableMenus", config);
    }

}
