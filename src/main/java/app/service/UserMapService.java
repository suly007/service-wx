package app.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户openId与姓名映射关系
 *
 * @author: Huanqd@2018-10-22 14:23
 */
@Component
@PropertySource(value = "classpath:user-map.yml",encoding = "UTF-8")
@ConfigurationProperties()
public class UserMapService {
    private Map<String, String> userMap = new HashMap<String, String>();


    public Map<String, String> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
    }

    public String getUserName(String openId){
        return userMap.get(openId);
    }
}
