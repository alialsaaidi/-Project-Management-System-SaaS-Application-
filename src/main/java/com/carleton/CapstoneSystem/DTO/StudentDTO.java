package com.carleton.CapstoneSystem.DTO;

import com.carleton.CapstoneSystem.models.Program;
import com.carleton.CapstoneSystem.models.Student;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.stream.Collectors;

public class StudentDTO extends UserDTO{

    @JsonProperty
    private ProjectDTO project;

    @JsonProperty
    private Program program;

    @JsonProperty
    private Set<ProjectDTO> appliedProjects;

    public StudentDTO() {

    }

    public StudentDTO(Student student) {
        super(student);

        if(student != null) {
            if(student.getProject()!=null) {
                this.project = new ProjectDTO(student.getProject());
            }
            this.program = student.getProgram();


            if (student.getAppliedProjects() != null) {
                this.appliedProjects = student.getAppliedProjects().stream().map(appliedProject -> {
                    return new ProjectDTO(appliedProject);
                }).collect(Collectors.toSet());
            }
        }
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    @Override
    public Program getProgram() {
        return program;
    }

    @Override
    public void setProgram(Program program) {
        this.program = program;
    }

    public Set<ProjectDTO> getAppliedProjects() {
        return appliedProjects;
    }

    public void setAppliedProjects(Set<ProjectDTO> appliedProjects) {
        this.appliedProjects = appliedProjects;
    }
}