import com.sudy.util.GitUtil;
import org.apache.el.parser.AstFalse;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileCopierTest {

    @Test
    public void test() throws IOException, InterruptedException {
        // 1. 获取指定提交的修改文件列表
        List<String> changedFiles = GitUtil.getChangedFilesViaCLI(
                "E:\\sudytech\\webpluspro\\webpluspromaster",
                "b09ac8fd"
        );
        System.out.println("修改的文件:");
        changedFiles.forEach(System.out::println);

        // 2. 定义源目录和目标目录
        Path sourceBaseDir = Paths.get("E:\\sudytech\\webpluspro\\webpluspromaster\\02src\\target\\webpluspro-3.0.7.1");
        Path outputDir = Paths.get("E:\\sudytech\\patch\\output");

        // 确保输出目录存在
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 3. 处理每个修改文件
        for (String filePath : changedFiles) {
            // 提取编译后文件的可能位置
            List<Path> compiledPaths = findCompiledPaths(filePath, sourceBaseDir);

            // 复制找到的编译文件
            for (Path compiledPath : compiledPaths) {
                if (Files.exists(compiledPath)) {
                    // 在输出目录保持相同的相对路径结构
                    Path relativePath = sourceBaseDir.relativize(compiledPath);
                    Path destination = outputDir.resolve(relativePath);

                    // 确保目标目录存在
                    Files.createDirectories(destination.getParent());

                    // 复制文件
                    Files.copy(compiledPath, destination, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("已复制: " + compiledPath + " -> " + destination);
                } else {
                    System.out.println("警告: 编译文件不存在 - " + compiledPath);
                }
            }
        }

        System.out.println("处理完成! 文件已保存到: " + outputDir);
    }

    /**
     * 根据源文件路径找到编译后的文件路径
     *
     * @param originalPath 原始文件路径 (Git返回的路径)
     * @param baseDir      基础目录 (target目录路径)
     * @return 可能的编译文件路径列表
     */
    private List<Path> findCompiledPaths(String originalPath, Path baseDir) {
        List<Path> compiledPaths = new ArrayList<>();

        // 根据文件类型确定编译位置
        if (originalPath.contains("src/main/java/") && originalPath.endsWith(".java")) {
            // Java文件 -> 编译到classes目录
            String classPath = originalPath
                    .replaceFirst("02src/src/main/java/", "classes/")
                    .replace(".java", ".class");
            compiledPaths.add(baseDir.resolve("WEB-INF").resolve(classPath));
        } else if (originalPath.contains("src/main/webapp/")) {
            // JSP文件 -> 复制到webplus目录
            String relativePath = originalPath.replaceFirst("02src/src/main/webapp/", "");
            compiledPaths.add(baseDir.resolve(relativePath));
        } else if (originalPath.endsWith(".properties") || originalPath.contains("src/main/resources/")) {
            // 配置文件 -> 复制到resources目录
            String relativePath = originalPath
                    .replaceFirst("02src/src/main/resources/", "");
            compiledPaths.add(baseDir.resolve("classes").resolve(relativePath));
        }

        // 处理可能的内部类
        if (originalPath.contains("src/main/java/") && originalPath.endsWith(".java")) {
            String classDirPath = originalPath
                    .replaceFirst("02src/src/main/java/", "classes/")
                    .replace(".java", "");

            Path classDir = baseDir.resolve("WEB-INF").resolve(classDirPath).getParent();

            // 添加可能的内部类文件
            if (classDir != null && Files.exists(classDir)) {
                try {
                    Files.walkFileTree(classDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.getFileName().toString().startsWith(classDirPath.substring(
                                    classDirPath.lastIndexOf('/') + 1) + "$") &&
                                    file.toString().endsWith(".class")) {
                                compiledPaths.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    System.err.println("扫描内部类时出错: " + e.getMessage());
                }
            }
        }

        // 添加原始文件本身（适用于资源文件等）
//        Path originalFullPath = Paths.get("E:\\sudytech\\webpluspro\\webpluspromaster").resolve(originalPath);
//        if (!compiledPaths.contains(originalFullPath) && Files.exists(originalFullPath)) {
//            compiledPaths.add(originalFullPath);
//        }

        return compiledPaths;
    }
    @Test
    public void test1() throws IOException, GitAPIException, InterruptedException {
        System.out.println(GitUtil.getFormattedCommits("E:\\sudytech\\webpluspro\\webpluspromaster", 20, false));
    }
}