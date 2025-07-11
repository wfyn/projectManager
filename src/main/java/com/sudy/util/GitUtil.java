package com.sudy.util;

import com.sudy.vo.HistoryCommitVO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GitUtil {

    /**
     * 获取格式化提交历史（封装为VO对象）
     *
     * @param repoPath  仓库路径
     * @param limit     数量限制
     * @param withFiles 是否包含变更文件列表
     */
    public static List<HistoryCommitVO> getFormattedCommits(String repoPath, int limit, boolean withFiles)
            throws IOException, InterruptedException {

        // 构建git log命令
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("log");
        command.add("--pretty=format:%H|%h|%an|%ad|%s");
        command.add("--date=iso");
        command.add("--shortstat");
        if (limit > 0) {
            command.add("-n");
            command.add(String.valueOf(limit));
        }

        ProcessBuilder pb = buildProcess(repoPath, command);
        Process process = pb.start();
        List<HistoryCommitVO> commits = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            HistoryCommitVO currentCommit = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(" ")) {
                    // 处理统计信息行（例如：" 2 files changed, 3 insertions(+)"）
                    if (currentCommit != null) {
                        currentCommit.setFileChanges(parseFileChanges(line));
                        if (withFiles) {
                            currentCommit.setChangedFiles(
                                    getChangedFilesViaCLI(repoPath, currentCommit.getCommitHash())
                            );
                        }
                    }
                } else if (!line.trim().isEmpty()) {
                    // 处理提交信息行
                    String[] parts = line.split("\\|", 5);
                    if (parts.length == 5) {
                        currentCommit = new HistoryCommitVO();
                        currentCommit.setCommitHash(parts[0]);
                        currentCommit.setShortHash(parts[1]);
                        currentCommit.setCommitAuthor(parts[2]);
                        currentCommit.setCommitTime(parts[3]);
                        currentCommit.setCommitMessage(parts[4]);
                        commits.add(currentCommit);
                    }
                }
            }
        }

        verifyProcessSuccess(process);
        return commits;
    }

    /**
     * 解析git shortstat输出的文件变更数量
     * 示例输入：" 2 files changed, 3 insertions(+)"
     */
    private static int parseFileChanges(String statLine) {
        String[] parts = statLine.split(" ");
        if (parts.length > 1 && parts[1].equals("files")) {
            return Integer.parseInt(parts[0].trim());
        }
        return 0;
    }

    /**
     * 获取单个提交的详细变更文件列表
     */
    public static List<String> getChangedFilesViaCLI(String repoPath, String commitHash)
            throws IOException, InterruptedException {
        ProcessBuilder pb = buildProcess(repoPath,
                Arrays.asList("git", "show", "--pretty=format:", "--name-only", commitHash));

        Process process = pb.start();
        List<String> files = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(files::add);
        }

        verifyProcessSuccess(process);
        return files;
    }

    // 以下是重构的公共方法
    private static ProcessBuilder buildProcess(String repoPath, List<String> command) {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("LANG", "en_US.UTF-8");
        env.put("LC_ALL", "en_US.UTF-8");
        env.put("GIT_PAGER", "");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(repoPath));
        pb.redirectErrorStream(true);
        pb.environment().putAll(env);
        return pb;
    }


    private static void verifyProcessSuccess(Process process)
            throws IOException, InterruptedException {
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("Git命令执行超时");
        }
        if (process.exitValue() != 0) {
            String errorOutput;
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                errorOutput = errorReader.lines().collect(Collectors.joining("\n"));
            }
            throw new IOException("Git命令失败 (退出码: " + process.exitValue() + ")。错误信息: " + errorOutput);
        }
    }
}