package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }

    private String getAuthenticatedUsername() {
        // Get the authentication object from security context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipal) {
            // If it's a UserPrincipal object, get the username from it
            return ((UserPrincipal) principal).getUsername();
        } else if (principal instanceof String) {
            // If it's already a string (username)
            return (String) principal;
        } else {
            throw new RuntimeException("Unable to get authenticated username");
        }
    }

    public TaskResponse createTask(TaskRequest taskRequest) {
        // Get the authenticated user's username from SecurityContext
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(taskRequest.getDueDate());
        task.setUser(user); // <<-- CRITICAL: Link the task to the user

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    public List<TaskResponse> getAllTasksForUser() {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        // Only find tasks belonging to this specific user
        return taskRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long taskId) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId + " for the current user"));
        // The above query ensures the task belongs to the user

        return mapToTaskResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest taskRequest) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(taskRequest.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    public void deleteTask(Long taskId) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }
}