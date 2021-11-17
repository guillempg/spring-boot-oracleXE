package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.AssignTeacherRequest;
import com.example.springjpaoracle.dto.SaveTeacherRequest;
import com.example.springjpaoracle.exception.TeacherAssignationException;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Teacher;
import com.example.springjpaoracle.model.TeacherAssignation;
import com.example.springjpaoracle.repository.CourseRepository;
import com.example.springjpaoracle.repository.TeacherAssignationRepository;
import com.example.springjpaoracle.repository.TeacherRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

public class TeacherService
{
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final TeacherAssignationRepository teacherAssignationRepository;


    public TeacherService(final TeacherRepository teacherRepository, final CourseRepository courseRepository, final TeacherAssignationRepository teacherAssignationRepository)
    {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.teacherAssignationRepository = teacherAssignationRepository;
    }

    @Transactional
    public TeacherAssignation assignTeacher(final AssignTeacherRequest assignTeacherRequest)
    {
        final Teacher teacher = teacherRepository.findByKeycloakId(assignTeacherRequest.getTeacherKeycloakId())
                .orElseThrow(() -> new TeacherAssignationException(assignTeacherRequest.getTeacherKeycloakId(), assignTeacherRequest.getCourseName()));

        final Course course = ServiceUtil.findOrCreateCourses(
                courseRepository,
                Arrays.asList(assignTeacherRequest.getCourseName())).get(0);

        return teacherAssignationRepository.findByTeacherIdAndCourseName(teacher.getId(), course.getName())
                .orElseGet(() -> teacherAssignationRepository.save(new TeacherAssignation().setTeacher(teacher).setCourse(course)));
    }

    public Teacher findOrSaveTeacher(final SaveTeacherRequest saveTeacherRequest)
    {
        Teacher teacher = teacherRepository.findByKeycloakId(saveTeacherRequest.getKeycloakId()).orElse(
                (Teacher) new Teacher().setKeycloakId(saveTeacherRequest.getKeycloakId()));

        return teacherRepository.save(teacher);
    }
}
