package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
//@ContextConfiguration(initializers = {TestContainersInitializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest
{
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @AfterEach
    void setUp()
    {
        try
        {
            courseRepository.deleteAll();
            studentRepository.deleteAll();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
        //@Sql(statements = {"insert into student (id, name, user_details_id, social_security_number) values (default, ?, ?, ?)"})
    void findStudentsNotRegisteredToCourse()
    {

        final Course paintPink = new Course().setName("Paint Pink");
        final Course paintBlue = new Course().setName("Paint Blue");
        courseRepository.save(paintPink);
        courseRepository.save(paintBlue);


        final Student student1 = new Student().setName("Pink Panther").setCourses(Arrays.asList(paintPink)).setSocialSecurityNumber("111-111-111");
        final Student student2 = new Student().setName("Mr. Egg").setCourses(Arrays.asList(paintBlue)).setSocialSecurityNumber("222-222-222");

        studentRepository.save(student1);
        studentRepository.save(student2);

        final List<Student> unregisteredToPaintBlue = studentRepository.findStudentsNotRegisteredToCourse("Paint Blue");
        assertTrue(unregisteredToPaintBlue.contains(student1));
    }
}