import com.sudy.util.SmUtil;
import org.junit.jupiter.api.Test;



public class SmTest {



    @Test
    public void test3() throws Exception {

//        String sm4 = SmUtil.sm4encrypt("341281200211185039");
//        System.out.println(sm4);
//        SmUtil.sm4decrypt(sm4);

        String sm4Decrypt = SmUtil.sm4decrypt("RMeUULPTTatMZV9iny/Il0WOpFiS4qHSosg2qPRXzScGgwEtb1fZtAcXBGQCyc1g");
//        RMeUULPTTatMZV9iny/Il0WOpFiS4qHSosg2qPRXzScGgwEtb1fZtAcXBGQCyc1g
        System.out.println(sm4Decrypt);



    }
}
