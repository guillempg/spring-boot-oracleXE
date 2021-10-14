package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.StudentCourseScore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCourseScoreResponse
{
    private String studentName;
    private String courseName;
    private Double score;

    public static StudentCourseScoreResponse from(final StudentCourseScore savedScore)
    {
        final StudentCourseScoreResponse resp = new StudentCourseScoreResponse();
        resp.setStudentName(savedScore.getStudent().getName());
        resp.setCourseName(savedScore.getCourse().getName());
        resp.setScore(savedScore.getScore());
        return resp;
    }
}
