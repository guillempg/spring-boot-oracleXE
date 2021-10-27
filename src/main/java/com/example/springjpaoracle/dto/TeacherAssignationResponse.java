package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.TeacherAssignation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherAssignationResponse
{
    private String teacherKeycloakId;
    private String courseName;

    public static TeacherAssignationResponse from(final TeacherAssignation assignation)
    {
        TeacherAssignationResponse resp = new TeacherAssignationResponse();
        resp.setTeacherKeycloakId(assignation.getTeacher().getKeycloakId());
        resp.setCourseName(assignation.getCourse().getName());
        return resp;
    }
}
