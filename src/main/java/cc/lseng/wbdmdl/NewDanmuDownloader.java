package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.DanMuConverter;
import cc.lseng.wbdmdl.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cc.lseng.wbdmdl.util.DanMuConverter.replaceEmoji;
import static cc.lseng.wbdmdl.util.StringUtil.unicodeToCn;

/**
 * @author LSeng
 * @date 2022/2/3 17:19
 */
public class NewDanmuDownloader {

    private static int t = 1;
    private static int total = 0;

    /**
     * @param filepath 保存位置，需要包括文件名
     * @param mid 直播mid
     * @param startTimeStamp 直播开始的时间戳(可随便填，不影响最终渲染结果，只影响输出文件每个弹幕发送的现实时间的时间戳)
     */
    public static void get(String filepath, String mid, long startTimeStamp){
        try {
            List<DanMu> result = new ArrayList<>();
            next(mid, startTimeStamp, 0, result);

            //保存弹幕文件
            DanMuConverter.convertToXML(filepath, result, startTimeStamp);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for(DanMu danmu : result){
                System.out.println(simpleDateFormat.format(new Date(danmu.time)) + " " + danmu.name + " : " + danmu.info);
            }

            System.out.println("弹幕文件已保存至"+new File(filepath).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void next(String mid, long startTimeStamp, long offset, List<DanMu> list) throws Exception {
        String url = "https://weibo.com/tv/aj/barrage/list?start_offset="+offset+"&mid="+mid;

        String origin = HttpUtil.sendGET(url);

//        System.out.println(origin);

        JSONObject root = new JSONObject(origin);
        if(root.getInt("code") == 100000){
            JSONObject data = root.getJSONObject("data");
            JSONArray array = data.getJSONArray("list");
            for(Object entry : array){
                if(entry instanceof JSONObject){
                    JSONObject danmuRaw = (JSONObject) entry;
                    JSONObject senderInfo = danmuRaw.getJSONObject("sender_info");
                    //获取弹幕发送者名称，实际渲染的时候不包含发送者名称
                    //如果没有nickname，则直接将其uid作为名称
                    //可以通过https://weibo.com/u/$uid$来访问该用户的微博空间(替换掉$uid$)
                    String name;
                    if(senderInfo.has("nickname")){
                        name = senderInfo.getString("nickname");
                    } else {
                        name = String.valueOf(senderInfo.getLong("uid"));
                    }
                    //弹幕的时间
                    long dOffset = danmuRaw.getInt("offset");

                    String content = unicodeToCn(danmuRaw.getString("content"));

                    //将emoji从html标签抽离出来
                    content = replaceEmoji(content);

                    DanMu danmu = new DanMu(name, content, startTimeStamp + dOffset);

                    list.add(danmu);
                    total++;
                }
            }

            System.out.println("已获取: "+total);

            if(Config.wait != 0) {
                if (t % Config.frequency == 0) {
                    Thread.sleep(Config.wait);
                }
                t++;
            }

            next(mid, startTimeStamp, offset + 30000L, list);
        }
    }

}
