package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.HttpUtil;

import java.io.IOException;

/**
 * @author LSeng
 * @date 2022/2/10 13:06
 */
public class CookieChecker {

    public static boolean check() throws IOException {
        String s = HttpUtil.sendGET("https://weibo.com/easonch");
        return !s.contains("Sina Visitor System");
    }

}
