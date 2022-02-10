package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.DanMuConverter;
import cc.lseng.wbdmdl.util.HttpUtil;
import cc.lseng.wbdmdl.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author LSeng
 * @date 2022/2/9 23:28
 */
public class OldDanmuDownloader {

    private static int t = 1;
    private static int total = 0;

    /**
     * @param filepath 保存位置，需要包括文件名
     * @param id 直播id
     * @param start 开始时间，示例：2020-5-9 17:21，注意会影响渲染结果
     * @throws Exception
     */
    public static void get(String filepath, String id, String start) throws Exception{
        List<DanMu> danMuList = new ArrayList<>();

        //递归获取所有评论
        next(danMuList, id, "0");

        //输出所有评论
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(DanMu danmu : danMuList){
            System.out.println(simpleDateFormat.format(new Date(danmu.time)) + " " + danmu.name + " : " + danmu.info);
        }

        long timeStamp = DanMuConverter.getTimeStamp(start);

        //转换为xml文件
        DanMuConverter.convertToXML(filepath, danMuList, timeStamp);

        System.out.println("弹幕文件已保存至"+new File(filepath).getAbsolutePath());
    }

    private static void next(List<DanMu> danMuList, String id, String max_id) throws Exception {
        String url = "https://weibo.com/ajax/statuses/buildComments?flow=1&is_reload=1&id="+id+"&is_show_bulletin=2&is_mix=0&max_id="+max_id+"&count=20";

        String origin = StringUtil.unicodeToCn(HttpUtil.sendGET(url));
//        System.out.println(origin);

        JSONObject root = new JSONObject(origin);
        JSONArray array = root.getJSONArray("data");

        for(Object obj : array){
            if(obj instanceof JSONObject){
                JSONObject danmuRaw = (JSONObject) obj;
                long timestamp = new Date(danmuRaw.getString("created_at")).getTime();
                String content = danmuRaw.getString("text");

                //将emoji从html标签抽离出来
                content = DanMuConverter.replaceEmoji(content);

                JSONObject userJSON = danmuRaw.getJSONObject("user");
                String user = userJSON.getString("name");

                danMuList.add(new DanMu(user, content, timestamp));
            }
            total++;
        }

        if(Config.wait != 0) {
            if (t % Config.frequency == 0) {
                Thread.sleep(Config.wait);
            }
            t++;
        }

        System.out.println("已获取: "+total);
        if (root.has("max_id")) {
            if (root.getLong("max_id") != 0) {
                String next_max_id = String.valueOf(root.getLong("max_id"));
                next(danMuList, id, next_max_id);
            } else {
                System.out.println("last_max_id: " + max_id);
                Thread.sleep(3000);
                //再次确认(因为有时候会抽风)
                origin = StringUtil.unicodeToCn(HttpUtil.sendGET(url));
                root = new JSONObject(origin);
                if (root.has("max_id") && root.getLong("max_id") != 0) {
                    String next_max_id = String.valueOf(root.getLong("max_id"));
                    next(danMuList, id, next_max_id);
                } else {
                    System.out.println("end");
                }
            }
        }
    }

}
