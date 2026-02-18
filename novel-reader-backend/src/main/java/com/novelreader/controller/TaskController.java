package com.novelreader.controller;

import com.novelreader.entity.CrawlerTask;
import com.novelreader.repository.CrawlerTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/tasks")
public class TaskController {

    @Autowired
    private CrawlerTaskRepository taskRepository;

    /**
     * 获取任务列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) {

        log.info("获取任务列表: page={}, size={}, platform={}, status={}", page, size, platform, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<CrawlerTask> taskPage;

        if (platform != null && !platform.isEmpty()) {
            taskPage = taskRepository.findByPlatformOrderByStartTimeDesc(platform, pageable);
        } else if (status != null && !status.isEmpty()) {
            taskPage = taskRepository.findByStatusOrderByStartTimeDesc(status, pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", taskPage.getContent());
        result.put("totalElements", taskPage.getTotalElements());
        result.put("totalPages", taskPage.getTotalPages());
        result.put("size", taskPage.getSize());
        result.put("number", taskPage.getNumber());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CrawlerTask> getTask(@PathVariable Long id) {
        log.info("获取任务详情: id={}", id);

        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<CrawlerTask> createTask(@RequestBody CrawlerTask task) {
        log.info("创建任务: platform={}, taskType={}", task.getPlatform(), task.getTaskType());

        CrawlerTask savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ResponseEntity<CrawlerTask> updateTask(@PathVariable Long id, @RequestBody CrawlerTask task) {
        log.info("更新任务: id={}, status={}", id, task.getStatus());

        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setStatus(task.getStatus());
                    existingTask.setEndTime(task.getEndTime());
                    existingTask.setTotalCount(task.getTotalCount());
                    existingTask.setSuccessCount(task.getSuccessCount());
                    existingTask.setFailCount(task.getFailCount());
                    existingTask.setLogs(task.getLogs());
                    existingTask.setErrorMessage(task.getErrorMessage());
                    return ResponseEntity.ok(taskRepository.save(existingTask));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
