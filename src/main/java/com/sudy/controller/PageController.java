package com.sudy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

/**
 * Thymeleaf 页面跳转控制器
 */
@Controller
public class PageController {

    /**
     * 首页 -> 重定向到仓库列表
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/repositories";
    }

    /**
     * 仓库列表页
     */
    @GetMapping("/repositories")
    public String repositories() {
        return "repository/list";
    }

    /**
     * 新增仓库页
     */
    @GetMapping("/repositories/add")
    public String addRepository() {
        return "repository/form";
    }

    /**
     * 编辑仓库页
     */
    @GetMapping("/repositories/edit/{id}")
    public String editRepository(@PathVariable Long id, Model model) {
        model.addAttribute("repoId", id);
        return "repository/form";
    }

    /**
     * 提交历史页（选择 commit 发起部署）
     */
    @GetMapping("/repositories/{id}/commits")
    public String commitHistory(@PathVariable Long id, Model model) {
        model.addAttribute("repoId", id);
        return "repository/commits";
    }

    /**
     * 任务列表页
     */
    @GetMapping("/tasks")
    public String tasks() {
        return "task/list";
    }

    /**
     * 任务详情页
     */
    @GetMapping("/tasks/{id}")
    public String taskDetail(@PathVariable Long id, Model model) {
        model.addAttribute("taskId", id);
        return "task/detail";
    }
}
