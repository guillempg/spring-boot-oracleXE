package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Course;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRepositoryTest extends RepositoryBaseTest
{
    @Autowired
    private CourseRepository repository;

    @Autowired
    private SessionFactory sessionFactory;

    @AfterEach
    void cleanup()
    {
        repository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(statements = {"insert into course (id, name, description) values (default, 'Redis 101', 'Learn Redis')",
            "insert into course (id, name, description) values (default, 'Redis 201', 'Master Redis')",
            "insert into course (id, name, description) values (default, 'Redis 301', 'Expert in Redis')"})
    void shouldCacheIn2Lvl()
    {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        List<Course> courses = repository.findByNameIgnoreCaseIn(List.of("Redis 101", "Redis 201", "Redis 301"));
        courses.forEach(c -> assertTrue(sessionFactory.getCache().contains(Course.class, c.getId())));
        assertThat(statistics.getSecondLevelCacheHitCount()).isEqualTo(3L);
        statistics.clear();
    }
}
