
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudy.util.GitUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileCopierTest {


    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void test() throws IOException, InterruptedException {
        executeCopy("5.0.1.5");
    }


    /**
     * 加载指定版本的配置
     *
     * @param version 版本号，如 "5.0.1.5"
     */
    private Config loadConfig(String version) throws IOException {
        List<Map<String, Config>> configList = loadConfigList();

        for (Map<String, Config> map : configList) {
            if (map.containsKey(version)) {
                return map.get(version);
            }
        }

        throw new IllegalArgumentException("找不到版本配置: " + version);
    }

    /**
     * 加载完整配置列表
     */
    private List<Map<String, Config>> loadConfigList() throws IOException {
        // 优先从外部文件加载
        Path externalConfig = Paths.get("config.json");
        if (Files.exists(externalConfig)) {
            return objectMapper.readValue(externalConfig.toFile(), new TypeReference<List<Map<String, Config>>>() {
            });
        }

        // 从 classpath 加载
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.json")) {
            if (is == null) {
                throw new IOException("找不到配置文件: config.json");
            }
            return objectMapper.readValue(is, new TypeReference<List<Map<String, Config>>>() {
            });
        }
    }


    /**
     * 执行文件复制
     */
    private void executeCopy(String version) throws IOException, InterruptedException {
        // 加载指定版本配置
        Config config = loadConfig(version);
        System.out.println("当前版本: " + version);
        System.out.println("配置信息: " + config);

        // 1. 获取 Git 变更文件
        List<String> changedFiles = GitUtil.getChangedFilesViaCLI(
                config.getProjectPath(),
                config.getCommitHash()
        );
        System.out.println("修改的文件:");
        changedFiles.forEach(System.out::println);

        // 2. 定义源目录和目标目录
        Path sourceBaseDir = Paths.get(config.getSourceBaseDir());
        Path outputDir = Paths.get(config.getOutputDir());

        // 确保输出目录存在
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 3. 处理每个修改文件
        for (String filePath : changedFiles) {
            List<Path> compiledPaths = findCompiledPaths(filePath, sourceBaseDir);

            for (Path compiledPath : compiledPaths) {
                if (Files.exists(compiledPath)) {
                    Path relativePath = sourceBaseDir.relativize(compiledPath);
                    Path destination = outputDir.resolve(relativePath);

                    Files.createDirectories(destination.getParent());
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


    /**
     * 配置类，对应 config.json 结构
     */
    public static class Config {
        private String projectPath;      // Git 项目路径
        private String commitHash;      // Git commit hash
        private String sourceBaseDir;   // 编译文件源目录
        private String outputDir;       // 输出目录

        // Getters and Setters
        public String getProjectPath() {
            return projectPath;
        }

        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }

        public String getCommitHash() {
            return commitHash;
        }

        public void setCommitHash(String commitHash) {
            this.commitHash = commitHash;
        }

        public String getSourceBaseDir() {
            return sourceBaseDir;
        }

        public void setSourceBaseDir(String sourceBaseDir) {
            this.sourceBaseDir = sourceBaseDir;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "projectPath='" + projectPath + '\'' +
                    ", commitHash='" + commitHash + '\'' +
                    ", sourceBaseDir='" + sourceBaseDir + '\'' +
                    ", outputDir='" + outputDir + '\'' +
                    '}';
        }
    }
}