package com.example.backend.controller;

import com.example.backend.dto.TaskRequest;
import com.example.backend.dto.TaskResponse;
import com.example.backend.dto.ErrorResponse;
import com.example.backend.model.Task;
import com.example.backend.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            return ResponseEntity.ok(taskService.getAllTasks());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            java.util.Optional<Task> opt = taskService.getTaskById(id);
            if (opt.isPresent()) {
                return ResponseEntity.ok(opt.get());
            }
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", "Task not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest taskRequest) {
        try {
            Task task = new Task();
            BeanUtils.copyProperties(taskRequest, task);
            Task savedTask = taskService.createTask(task);
            TaskResponse taskResponse = new TaskResponse();
            BeanUtils.copyProperties(savedTask, taskResponse);
            return ResponseEntity.ok(taskResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskRequest taskRequest) {
        try {
            Task updatedTask = taskService.updateTask(id, new Task());
            BeanUtils.copyProperties(taskRequest, updatedTask);
            TaskResponse taskResponse = new TaskResponse();
            BeanUtils.copyProperties(updatedTask, taskResponse);
            return ResponseEntity.ok(taskResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
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
            Long userId = Long.valueOf(authentication.getName());
            return ResponseEntity.ok(taskService.getTasksByUserId(userId));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token: " + authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<?> markTaskAsFinished(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = Long.valueOf(authentication.getName());
            return ResponseEntity.ok(taskService.markTaskAsFinished(id, userId));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(400, "Bad Request", "Invalid user id in token: " + authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(new ErrorResponse(403, "Forbidden", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }
}