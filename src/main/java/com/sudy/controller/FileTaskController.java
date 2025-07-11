package com.sudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sudy.common.Result;
import com.sudy.dto.FileAddDTO;
import com.sudy.dto.PageDTO;
import com.sudy.entity.FileDetail;
import com.sudy.entity.FileTask;
import com.sudy.entity.GitRepository;
import com.sudy.mapper.FileDetailMapper;
import com.sudy.mapper.FileTaskMapper;
import com.sudy.mapper.GitRepositoryMapper;
import com.sudy.service.AsyncFileTaskService;
import com.sudy.util.GitUtil;
import com.sudy.vo.HistoryCommitVO;
import com.sudy.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FileTaskController {
    @Autowired
    private final AsyncFileTaskService asyncFileTaskService;

    @Autowired
    public FileTaskController(AsyncFileTaskService asyncFileTaskService) {
        this.asyncFileTaskService = asyncFileTaskService;
    }

    @Autowired
    private FileTaskMapper fileTaskMapper;

    @Autowired
    private FileDetailMapper fileDetailMapper;

    @Autowired
    private GitRepositoryMapper gitRepositoryMapper;

    /**
     * 创建文件处理任务
     */
    @PostMapping("/create")
    public Result createTask(@RequestBody FileAddDTO FileAddDTO) {

        return Result.ok(asyncFileTaskService.createTask(FileAddDTO));
    }

    /**
     * 获取任务列表
     */
    @PostMapping("/list")
    public Result listTasks(@RequestBody PageDTO pageDTO) {

        QueryWrapper<FileTask> query = new QueryWrapper<>();


        System.out.println("status");
        query.orderByDesc("created_at");
        Page<FileTask> fileTaskPage = fileTaskMapper.selectPage(new Page<>(pageDTO.getPage(), pageDTO.getSize()), query);

        System.out.println();
        System.out.println(query.getCustomSqlSegment());
        List<FileTask> records = fileTaskPage.getRecords();

        records.forEach(task -> {
            task.setFileDetails(fileDetailMapper.selectList(
                    new QueryWrapper<FileDetail>().eq("task_id", task.getId())
            ));
        });
        PageVO<FileTask> taskPageVO = new PageVO<>();

        taskPageVO.setList(records);
        taskPageVO.setTotal(Long.valueOf(fileTaskMapper.selectCount(query)));

        return Result.ok(taskPageVO);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result getTaskDetail(@PathVariable Long id) {
        FileTask task = fileTaskMapper.selectById(id);
        if (task != null) {
            task.setFileDetails(fileDetailMapper.selectList(
                    new QueryWrapper<FileDetail>().eq("task_id", id)
            ));
        }
        return Result.ok(task);
    }

    /**
     * 重新执行任务
     */
    @PostMapping("/{id}/retry")
    public Result retryTask(@PathVariable Long id) {
        FileTask task = fileTaskMapper.selectById(id);
        if (task == null) {
            return Result.ok("任务不存在");
        }

        if ("RUNNING".equals(task.getStatus())) {
            return Result.ok("任务正在执行中，无法重新执行");
        }

        // 重置任务状态
        FileTask update = new FileTask();
        update.setId(id);
        update.setStatus("PENDING");
        update.setProcessedAt(null);
        update.setErrorInfo(null);
        fileTaskMapper.updateById(update);

        // 重新执行任务
        asyncFileTaskService.executeTask(id);
        return Result.ok("任务已重新执行");
    }

    //创建仓库
    @PostMapping("/createRepo")
    public Result createRepo(@RequestBody GitRepository repoAddDTO) {

        int insert = gitRepositoryMapper.insert(repoAddDTO);
        return Result.ok();
    }

    //更新
    @PostMapping("/updateRepo")
    public Result updateRepo(@RequestBody GitRepository repoAddDTO) {

        int insert = gitRepositoryMapper.updateById(repoAddDTO);
        return Result.ok();
    }

    //获取仓库列表
    @GetMapping("/repositories")
    public Result createRepo() {

        List<GitRepository> list = gitRepositoryMapper.selectList(null);
        return Result.ok(list);
    }

    @PostMapping("/getHistoryList/{id}")
    public Result getHistoryList(@PathVariable Long id) throws IOException, InterruptedException {

        List<HistoryCommitVO> list = GitUtil.getFormattedCommits(gitRepositoryMapper.selectById(id).getRepoPath(), 10, true);
        return Result.ok(list);
    }
}