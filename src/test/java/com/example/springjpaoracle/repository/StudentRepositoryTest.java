package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.model.StudentRegistration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentRepositoryTest extends RepositoryBaseTest
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

        final Student studentRegisteredToPink = new Student().setKeycloakId("111-111-111");
        final Student studentRegisteredToBlue = new Student().setKeycloakId("222-222-222");

        final StudentRegistration pinkPantherEnrolled = new StudentRegistration().setStudent(studentRegisteredToPink).setCourse(paintPink);
        final StudentRegistration mrEggEnrolled = new StudentRegistration().setStudent(studentRegisteredToBlue).setCourse(paintBlue);

        studentRegisteredToPink.setRegistrations(Arrays.asList(pinkPantherEnrolled));
        studentRegisteredToBlue.setRegistrations(Arrays.asList(mrEggEnrolled));

        studentRepository.save(studentRegisteredToPink);
        studentRepository.save(studentRegisteredToBlue);

        final List<Student> unregisteredToPaintBlue = studentRepository.findStudentsNotRegisteredToCourse("Paint Blue");
        assertTrue(unregisteredToPaintBlue.contains(studentRegisteredToPink));
        assertFalse(unregisteredToPaintBlue.contains(studentRegisteredToBlue));
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
