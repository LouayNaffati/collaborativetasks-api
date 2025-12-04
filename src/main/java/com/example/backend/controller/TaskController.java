package com.example.backend.controller;

import com.example.backend.dto.TaskRequest;
import com.example.backend.dto.TaskResponse;
import com.example.backend.dto.ErrorResponse;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.model.Project;
import com.example.backend.service.TaskService;
import com.example.backend.service.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    
    @Autowired
    private ProjectService projectService;

    @Autowired  // Add this
    private UserRepository userRepository;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            boolean isAdmin = isAdmin(authentication);
            
            if (isAdmin) {
                List<TaskResponse> responses = taskService.getAllTasks().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
            } else {
                List<TaskResponse> responses = taskService.getTasksByUserId(userId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            boolean isAdmin = isAdmin(authentication);
            
            java.util.Optional<Task> opt = taskService.getTaskById(id);
            if (opt.isPresent()) {
                Task task = opt.get();
                
                // Check authorization
                if (!isAdmin && !task.getUserId().equals(userId)) {
                    // Also check if user is a collaborator on the project
                    if (task.getProject() == null || task.getProject().getCollaborators().stream()
                        .noneMatch(user -> user.getId().equals(userId))) {
                        return ResponseEntity.status(403).body(
                            new ErrorResponse(403, "Forbidden", "You don't have permission to access this task"));
                    }
                }
                
                return ResponseEntity.ok(convertToResponse(task));
            }
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", "Task not found with id: " + id));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest taskRequest, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            
            Task task = new Task();
            BeanUtils.copyProperties(taskRequest, task);
            
            // Set user ID from authentication
            task.setUserId(userId);
            
            Task savedTask;
            
            if (taskRequest.getProjectId() != null) {
                // Create task with project relationship
                savedTask = taskService.createTaskForProject(task, taskRequest.getProjectId());
            } else {
                // Create standalone task
                savedTask = taskService.createTask(task);
            }
            
            TaskResponse taskResponse = convertToResponse(savedTask);
            return ResponseEntity.ok(taskResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskRequest taskRequest, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            
            // First get the existing task to check ownership
            Task existingTask = taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
            
            // Check authorization
            if (!existingTask.getUserId().equals(userId) && !isAdmin(authentication)) {
                return ResponseEntity.status(403).body(
                    new ErrorResponse(403, "Forbidden", "You don't have permission to update this task"));
            }
            
            Task updatedTask = new Task();
            BeanUtils.copyProperties(taskRequest, updatedTask);
            
            // Preserve user ID and project if not changing
            if (updatedTask.getUserId() == null) {
                updatedTask.setUserId(existingTask.getUserId());
            }
            
            // If project ID is provided in request, set the project
            if (taskRequest.getProjectId() != null && existingTask.getProject() == null) {
                // Task is being assigned to a project - need to check if user is collaborator
                boolean isCollaborator = projectService.getProjectById(taskRequest.getProjectId(), 
                    authentication.getName(), isAdmin(authentication)) != null;
                
                if (!isCollaborator && !isAdmin(authentication)) {
                    return ResponseEntity.status(403).body(
                        new ErrorResponse(403, "Forbidden", "You are not a collaborator on this project"));
                }
            }
            
            Task result = taskService.updateTask(id, updatedTask);
            TaskResponse taskResponse = convertToResponse(result);
            return ResponseEntity.ok(taskResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            
            // First get the existing task to check ownership
            Task existingTask = taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
            
            // Check authorization - only task owner or admin can delete
            if (!existingTask.getUserId().equals(userId) && !isAdmin(authentication)) {
                return ResponseEntity.status(403).body(
                    new ErrorResponse(403, "Forbidden", "You don't have permission to delete this task"));
            }
            
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserTasks(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<TaskResponse> responses = taskService.getTasksByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            boolean isAdmin = isAdmin(authentication);
            
            // Check if user has access to this project
            com.example.backend.dto.ProjectDto project = projectService.getProjectById(projectId, 
                authentication.getName(), isAdmin);
            
            if (project == null) {
                return ResponseEntity.status(403).body(
                    new ErrorResponse(403, "Forbidden", "You don't have access to this project"));
            }
            
            List<TaskResponse> responses;
            if (isAdmin) {
                responses = taskService.getTasksByProjectId(projectId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            } else {
                responses = taskService.getTasksByUserIdAndProjectId(userId, projectId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }
    
    @GetMapping("/user/project/{projectId}")
    public ResponseEntity<?> getUserTasksByProject(@PathVariable Long projectId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            boolean isAdmin = isAdmin(authentication);
            
            // Check if user has access to this project
            com.example.backend.dto.ProjectDto project = projectService.getProjectById(projectId, 
                authentication.getName(), isAdmin);
            
            if (project == null) {
                return ResponseEntity.status(403).body(
                    new ErrorResponse(403, "Forbidden", "You don't have access to this project"));
            }
            
            List<TaskResponse> responses = taskService.getTasksByUserIdAndProjectId(userId, projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<?> markTaskAsFinished(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Task updatedTask = taskService.markTaskAsFinished(id, userId);
            TaskResponse taskResponse = convertToResponse(updatedTask);
            return ResponseEntity.ok(taskResponse);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized") || e.getMessage().contains("collaborator")) {
                return ResponseEntity.status(403).body(new ErrorResponse(403, "Forbidden", e.getMessage()));
            }
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestParam String status, 
                                              Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Task updatedTask = taskService.updateTaskStatus(id, status, userId);
            TaskResponse taskResponse = convertToResponse(updatedTask);
            return ResponseEntity.ok(taskResponse);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized") || e.getMessage().contains("collaborator")) {
                return ResponseEntity.status(403).body(new ErrorResponse(403, "Forbidden", e.getMessage()));
            }
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    // Helper methods
    private Long getUserIdFromAuth(Authentication authentication) {
    String username = authentication.getName();
    // Fetch the user from database to get their ID
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    return user.getId();
}
    
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    
    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        BeanUtils.copyProperties(task, response);
        
        // Set project ID if task has a project
        if (task.getProject() != null) {
            response.setProjectId(task.getProject().getId());
            response.setProjectName(task.getProject().getName());
        }
        
        return response;
    }
}