package cc.lseng.wbdmdl.util;

/**
 * @author LSeng
 * @date 2022/2/8 16:14
 */
public class StringUtil {

    public static String unicodeToCn(String info) {
        String[] secs = info.split("\\\\u");
        if (secs.length <= 1) {
            return info;
        }
        String res = info;
        for (int i = 1; i < secs.length; i++) {
            String s = secs[i];
            char c = (char) Integer.valueOf(s.substring(0, 4), 16).intValue();
            res = res.replaceAll("\\\\u"+s.substring(0, 4), c+"");
        }
        return res;
    }

}
