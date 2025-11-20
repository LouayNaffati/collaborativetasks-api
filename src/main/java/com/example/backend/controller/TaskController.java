package com.example.backend.controller;

import com.example.backend.dto.TaskRequest;
import com.example.backend.dto.TaskResponse;
import com.example.backend.model.Task;
import com.example.backend.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest taskRequest) {
        Task task = new Task();
        BeanUtils.copyProperties(taskRequest, task);
        Task savedTask = taskService.createTask(task);
        TaskResponse taskResponse = new TaskResponse();
        BeanUtils.copyProperties(savedTask, taskResponse);
        return taskResponse;
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest taskRequest) {
        try {
            Task updatedTask = taskService.updateTask(id, new Task());
            BeanUtils.copyProperties(taskRequest, updatedTask);
            TaskResponse taskResponse = new TaskResponse();
            BeanUtils.copyProperties(updatedTask, taskResponse);
            return ResponseEntity.ok(taskResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    public List<Task> getUserTasks(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return taskService.getTasksByUserId(userId);
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Task> markTaskAsFinished(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        try {
            return ResponseEntity.ok(taskService.markTaskAsFinished(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }
}