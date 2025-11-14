package com.example.backend.service;

import com.example.backend.dto.ProjectDto;
import com.example.backend.model.Project;
import com.example.backend.model.User;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<ProjectDto> getProjectsForUser(String username) {
        return projectRepository.findByCollaboratorsUsername(username).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProjectDto getProjectById(Long id, String requesterUsername, boolean requesterIsAdmin) {
        Project project = projectRepository.findById(id).get();
        if (!requesterIsAdmin && project.getCollaborators().stream().noneMatch(u -> u.getUsername().equals(requesterUsername))) {
            return null;
        }
        return toDto(project);
    }

    public ProjectDto createProject(ProjectDto dto, String creatorUsername) {
        Project project = new Project();
        project.setImgUrl(dto.getImgUrl());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());

        Set<User> collaborators = dto.getCollaborators().stream()
                .map(id -> userRepository.findById(id).get())
                .collect(Collectors.toSet());

        User creator = userRepository.findByUsername(creatorUsername).get();
        collaborators.add(creator);
        project.setCollaborators(collaborators);

        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    public ProjectDto updateProject(Long id, ProjectDto dto, String requesterUsername, boolean requesterIsAdmin) {
        Project project = projectRepository.findById(id).get();
        if (!requesterIsAdmin && project.getCollaborators().stream().noneMatch(u -> u.getUsername().equals(requesterUsername))) {
            return null;
        }

        project.setImgUrl(dto.getImgUrl());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());

        if (!dto.getCollaborators().isEmpty()) {
            Set<User> collaborators = dto.getCollaborators().stream()
                    .map(userId -> userRepository.findById(userId).get())
                    .collect(Collectors.toSet());
            project.setCollaborators(collaborators);
        }

        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    public void deleteProject(Long id, String requesterUsername, boolean requesterIsAdmin) {
        Project project = projectRepository.findById(id).get();
        if (!requesterIsAdmin && project.getCollaborators().stream().noneMatch(u -> u.getUsername().equals(requesterUsername))) {
            return;
        }
        projectRepository.deleteById(id);
    }

    private ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setImgUrl(project.getImgUrl());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCollaborators(project.getCollaborators().stream().map(User::getId).collect(Collectors.toSet()));
        return dto;
    }
}
