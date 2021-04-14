package cn.cbsd.cjyfunctionlib.func_server.controller;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;

/**
 * @author Created by WZW on 2021-04-13 16:20.
 * @description
 */

@Controller
public class PageController {


//    @GetMapping(path = "/")
//    public String index() {
//        // Equivalent to [return "/index"].
//        return "forward:/index.html";
//    }

    @GetMapping(path = "/reboot")
    String reboot() {
        CJYHelper.getInstance().reboot();
        return "重启命令已到达";
    }
}
