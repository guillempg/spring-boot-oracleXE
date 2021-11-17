package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Course;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.codec.MarshallingCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
@Import({CourseRepositoryTest.TestConfig.class, RedisAutoConfiguration.class})
class CourseRepositoryTest extends RepositoryBaseTest
{
    @TestConfiguration
    public static class TestConfig
    {
        @Bean
        public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory factory)
        {
            MarshallingCodec marshallingCodec = new MarshallingCodec();
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(factory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashKeySerializer(new RedisSerializer<>()
            {
                @Override
                public byte[] serialize(final Object o) throws SerializationException
                {
                    return null;
                }

                @Override
                @SneakyThrows
                public Object deserialize(final byte[] bytes) throws SerializationException
                {
                    return marshallingCodec.getMapKeyDecoder().decode(Unpooled.wrappedBuffer(bytes), null);
                }
            });
            redisTemplate.setHashValueSerializer(new RedisSerializer<>()
            {
                @Override
                @SneakyThrows
                public byte[] serialize(final Object o) throws SerializationException
                {
                    return null;
                }

                @Override
                @SneakyThrows
                public Object deserialize(final byte[] bytes) throws SerializationException
                {
                    byte[] filteredByteArray = Arrays.copyOfRange(bytes, 16, bytes.length);
                    return marshallingCodec.getMapValueDecoder().decode(Unpooled.wrappedBuffer(filteredByteArray), null);
                }
            });
            return redisTemplate;
        }
    }

    @Autowired
    private CourseRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void cleanup()
    {
        repository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(statements = {"insert into course (id, name, description) values (default, 'Redis 101', 'Learn Redis')"})
    void shouldFindById()
    {
        Course course = repository.findAll().stream().findFirst().orElseThrow();
        Optional<Course> byId = repository.findById(course.getId());
        redisTemplate.opsForHash().entries("courseCache");
        assertThat(course).isNotNull();
    }
}
