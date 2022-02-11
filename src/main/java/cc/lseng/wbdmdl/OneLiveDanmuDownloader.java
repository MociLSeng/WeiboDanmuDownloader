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
 * @date 2022/2/8 22:28
 */
public class OneLiveDanmuDownloader {

    private static int t = 1;
    private static int total = 0;
    private static int noDanmuTick = 0;

    /**
     * @param filepath 保存位置，需要包括文件名
     * @param scid 直播scid
     * @param startTimeStamp 直播开始的时间戳(可随便填，不影响最终渲染结果，只影响输出文件每个弹幕发送的现实时间的时间戳)
     */
    public static void get(String filepath, String scid, long startTimeStamp){
        try {
            List<DanMu> result = new ArrayList<>();
            next(scid, startTimeStamp, 0, result);

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

    public static void next(String scid, long startTimeStamp, long ts, List<DanMu> list) throws Exception {
        String url = "https://www.yizhibo.com/live/h5api/get_playback_event?scid="+scid+"&ts="+ts;

        String origin = HttpUtil.sendGET(url);

//        System.out.println(origin);

        JSONObject root = new JSONObject(origin);

        JSONObject data = root.getJSONObject("data");
        JSONArray array = data.getJSONArray("list");

        if(array.length() > 0) {
            long maxTs = ts;
            for (Object entry : array) {
                if (entry instanceof JSONObject) {
                    JSONObject danmuRaw = (JSONObject) entry;
                    //获取弹幕发送者名称，实际渲染的时候不包含发送者名称
                    //如果没有nickname，则直接将其id作为名称
                    //一直播的nickname可能为null，所以还需判断是否为null
                    String name;
                    if (danmuRaw.has("nickname") && !danmuRaw.isNull("nickname")) {
                        name = unicodeToCn(danmuRaw.getString("nickname"));
                    } else {
                        name = String.valueOf(danmuRaw.getLong("id"));
                    }
                    //弹幕的时间
                    long dTs = danmuRaw.getInt("ts");
                    maxTs = Math.max(maxTs, dTs);

                    String content = unicodeToCn(danmuRaw.getString("content"));

                    //将emoji从html标签抽离出来
                    content = replaceEmoji(content);

                    DanMu danmu = new DanMu(name, content, startTimeStamp + dTs);

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

            noDanmuTick = 0;
            next(scid, startTimeStamp, maxTs + 1L, list);
        } else {
            //超过配置中设定的无弹幕时间，停止获取弹幕
            //这是为了防止一段时间内没人发弹幕，导致中断，获取不完整
            if(noDanmuTick >= Config.one_skip_no_danmu / 10){
                return;
            }
            noDanmuTick++;
            next(scid, startTimeStamp, ts + 10001L, list);
        }
    }
    
}
