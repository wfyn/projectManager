import com.sudy.ProjectManagerApplication;
import com.sudy.service.AsyncFileTaskService;
import com.sudy.service.impl.GitRepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ProjectManagerApplication.class)
public class SpringTest {

    @Autowired
    private GitRepositoryService gitRepository;

    @Autowired
    private AsyncFileTaskService asyncFileTaskService;

    @Test
    public void test() throws InterruptedException {
        asyncFileTaskService.executeTask(13L);    }
}
