package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.StudentCourseScore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class StudentCourseScoreResponse
{
    private String studentKeycloakId;
    private String teacherKeycloakId;
    private String courseName;
    private Double score;

    public static StudentCourseScoreResponse from(final StudentCourseScore savedScore)
    {
        final var resp = new StudentCourseScoreResponse();
        resp.setStudentKeycloakId(savedScore.getRegistration().getStudent().getKeycloakId());
        resp.setTeacherKeycloakId(savedScore.getTeacher().getKeycloakId());
        resp.setCourseName(savedScore.getRegistration().getCourse().getName());
        resp.setScore(savedScore.getScore());
        return resp;
    }

    public static List<StudentCourseScoreResponse> from(final Collection<StudentCourseScore> scores)
    {
        final List<StudentCourseScoreResponse> resp = new ArrayList<>();
        scores.forEach(s -> resp.add(StudentCourseScoreResponse.from(s)));
        return resp;
    }
}
