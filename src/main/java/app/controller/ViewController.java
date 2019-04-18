package app.controller;

import app.pojo.Stocks;
import app.task.MonitorTask;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author faith.huan 2019-04-18 9:38
 */
@Controller
public class ViewController {

    @RequestMapping("/group")
    public String group(){


        return "group";
    }


    @RequestMapping("/getCurrentInfo")
    @ResponseBody
    public Map<String,String> getCurrentInfo(){

        return MonitorTask.getInfo();
    }

}
