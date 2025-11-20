package com.example.backend.service;

import com.example.backend.model.Task;
import com.example.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        when(taskRepository.save(task)).thenReturn(task);

        Task createdTask = taskService.createTask(task);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testGetTaskById() {
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> foundTask = taskService.getTaskById(1L);

        assertTrue(foundTask.isPresent());
        assertEquals(1L, foundTask.get().getId());
        verify(taskRepository, times(1)).findById(1L);
    }
}