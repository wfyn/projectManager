package com.sudy.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitUtil {

    public static List<String> getChangedFilesInCommit(String repoPath, String commitHash) throws IOException, GitAPIException {
        List<String> filePaths = new ArrayList<>();

        try (Repository repository = Git.open(new File(repoPath)).getRepository();
             Git git = new Git(repository)) {

            // 获取指定提交
            Iterable<RevCommit> commits = git.log().add(repository.resolve(commitHash)).call();
            RevCommit commit = commits.iterator().next();

            // 如果这不是第一次提交，获取与父提交的差异
            if (commit.getParentCount() > 0) {
                RevCommit parent = commit.getParent(0);
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.addTree(parent.getTree());
                    treeWalk.setRecursive(true);

                    while (treeWalk.next()) {
                        if (treeWalk.getFileMode(0) != treeWalk.getFileMode(1)) {
                            filePaths.add(treeWalk.getPathString());
                        }
                    }
                }
            } else {
                // 第一次提交，列出所有文件
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);

                    while (treeWalk.next()) {
                        filePaths.add(treeWalk.getPathString());
                    }
                }
            }
        }

        return filePaths;
    }

    public static void main(String[] args) {
        try {
            List<String> changedFiles = getChangedFilesInCommit("E:\\sudytech\\projectManager\\projectManager", "HEAD");
            System.out.println("Changed files:");
            changedFiles.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}