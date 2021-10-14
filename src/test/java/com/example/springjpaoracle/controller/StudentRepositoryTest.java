package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.TestContainersInitializer;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.repository.CourseRepository;
import com.example.springjpaoracle.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(initializers = {TestContainersInitializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("slow")
class StudentRepositoryTest
{
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp()
    {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @AfterEach
    void cleanup()
    {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
        //@Sql(statements = {"insert into student (id, name, user_details_id, social_security_number) values (default, ?, ?, ?)"})
    void findStudentsNotRegisteredToCourse()
    {

        final Course paintPink = new Course().setName("Paint Pink");
        final Course paintBlue = new Course().setName("Paint Blue");
        courseRepository.save(paintPink);
        courseRepository.save(paintBlue);


        final Student studentRegisteredToPink = new Student().setName("Pink Panther").setCourses(Arrays.asList(paintPink)).setSocialSecurityNumber("111-111-111");
        final Student studentRegisteredToBlue = new Student().setName("Mr. Egg").setCourses(Arrays.asList(paintBlue)).setSocialSecurityNumber("222-222-222");

        studentRepository.save(studentRegisteredToPink);
        studentRepository.save(studentRegisteredToBlue);

        final List<Student> unregisteredToPaintBlue = studentRepository.findStudentsNotRegisteredToCourse("Paint Blue");
        assertTrue(unregisteredToPaintBlue.contains(studentRegisteredToPink));
        assertFalse(unregisteredToPaintBlue.contains(studentRegisteredToBlue));
    }
}