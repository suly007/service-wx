import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * 类
 *
 * @author: Huanqd@2018-10-22 16:13
 */
public class PropertiesTest {

@Test
public void test(){

    EncodedResource resource = new EncodedResource(new ClassPathResource("user-map.yml"),"UTF-8");

    try {
        Properties properties= PropertiesLoaderUtils.loadProperties(resource);
        System.out.println(properties);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
