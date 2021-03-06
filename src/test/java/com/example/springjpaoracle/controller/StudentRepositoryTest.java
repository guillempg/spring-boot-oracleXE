package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.config.OracleTestContainersInitializer;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.model.StudentRegistration;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(initializers = {OracleTestContainersInitializer.class})
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

        final Student studentRegisteredToPink = (Student) new Student().setKeycloakId("111-111-111");
        final Student studentRegisteredToBlue = (Student) new Student().setKeycloakId("222-222-222");

        final StudentRegistration pinkPantherEnrolled = new StudentRegistration().setStudent(studentRegisteredToPink).setCourse(paintPink);
        final StudentRegistration mrEggEnrolled = new StudentRegistration().setStudent(studentRegisteredToBlue).setCourse(paintBlue);

        studentRegisteredToPink.setRegistrations(Arrays.asList(pinkPantherEnrolled));
        studentRegisteredToBlue.setRegistrations(Arrays.asList(mrEggEnrolled));

        studentRepository.save(studentRegisteredToPink);
        studentRepository.save(studentRegisteredToBlue);

        final List<Student> unregisteredToPaintBlue = studentRepository.findStudentsNotRegisteredToCourse("Paint Blue");
        assertThat(unregisteredToPaintBlue).containsExactly(studentRegisteredToPink);
    }

    @Test
    void createCourseIfNotFound()
    {
        final Course course = new Course().setName("a course");
        courseRepository.save(course);

        final Optional<Course> existingCourse = courseRepository.findByNameIgnoreCase("a course");
        assertThat(existingCourse).isNotEmpty();
        final Optional<Course> nonExistingCourse = courseRepository.findByNameIgnoreCase("a non previously existing course");
        assertThat(nonExistingCourse).isEmpty();

    }
}
