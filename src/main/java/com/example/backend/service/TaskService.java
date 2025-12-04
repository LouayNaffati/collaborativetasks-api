package com.example.backend.service;

import com.example.backend.model.Task;
import com.example.backend.model.Project;
import com.example.backend.model.User;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.ProjectRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    public Task createTaskForProject(Task task, Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        
        // Verify user is a collaborator on the project
        boolean isCollaborator = project.getCollaborators().stream()
            .anyMatch(user -> user.getId().equals(task.getUserId()));
        
        if (!isCollaborator) {
            throw new RuntimeException("User with id " + task.getUserId() + " is not a collaborator on this project");
        }
        
        task.setProject(project);
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setStatus(updatedTask.getStatus());
            
            // Update project if changed
            if (updatedTask.getProject() != null) {
                task.setProject(updatedTask.getProject());
            }
            
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }
    
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }
    
    public List<Task> getTasksByUserIdAndProjectId(Long userId, Long projectId) {
        return taskRepository.findByUserIdAndProjectId(userId, projectId);
    }

    public Task markTaskAsFinished(Long taskId, Long userId) {
        return taskRepository.findById(taskId).map(task -> {
            // Check if user owns the task
            if (!task.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: User does not own this task");
            }
            
            // Also verify user is still a collaborator on the project
            if (task.getProject() != null) {
                boolean isCollaborator = task.getProject().getCollaborators().stream()
                    .anyMatch(user -> user.getId().equals(userId));
                
                if (!isCollaborator) {
                    throw new RuntimeException("User is no longer a collaborator on this project");
                }
            }
            
            task.setStatus("Finished");
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }
    
    public Task updateTaskStatus(Long taskId, String status, Long userId) {
        return taskRepository.findById(taskId).map(task -> {
            // Check if user is a collaborator on the project
            if (task.getProject() != null) {
                boolean isCollaborator = task.getProject().getCollaborators().stream()
                    .anyMatch(user -> user.getId().equals(userId));
                
                if (!isCollaborator) {
                    throw new RuntimeException("User is not a collaborator on this project");
                }
            } else {
                // For tasks without project, check ownership
                if (!task.getUserId().equals(userId)) {
                    throw new RuntimeException("Unauthorized: User does not own this task");
                }
            }
            
            task.setStatus(status);
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }
    
    public void deleteTasksByProjectId(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        taskRepository.deleteAll(tasks);
    }
}