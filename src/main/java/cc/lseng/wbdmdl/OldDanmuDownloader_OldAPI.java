package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.DanMuConverter;
import cc.lseng.wbdmdl.util.HttpUtil;
import cc.lseng.wbdmdl.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OldDanmuDownloader_OldAPI {

    private static Calendar calendar = Calendar.getInstance();
    private static boolean outputWhenLoading = true;
    private static boolean outputWhenLoaded = true;

    public static void get(String filepath, String id, String start) throws Exception {
        String url = "https://weibo.com/aj/v6/comment/big?ajwvr=6&id="+id+"&filter=all&from=singleWeiBo";

        String origin = StringUtil.unicodeToCn(HttpUtil.sendGET(url));

        List<DanMu> danMuList = new ArrayList<>();

        //递归获取所有评论
        next(origin, danMuList, id, 1);

        //输出评论
        if(outputWhenLoaded) {
            for(DanMu danMu : danMuList){
                System.out.println("{name="+danMu.name+", info="+danMu.info+", time="+danMu.time+"}");
            }
        }

        long timeStamp = DanMuConverter.getTimeStamp(start);

        //重设弹幕出现的时间
        DanMuConverter.resetTime(danMuList, timeStamp);

        //转换为xml文件
        DanMuConverter.convertToXML(filepath, danMuList, timeStamp);
    }

    private static void next(String next, List<DanMu> danMuList, String id, int page) throws Exception {
        String text = next;
        String text2 = next;

        System.out.println(text);

        int index;
        while ((index = text.indexOf("<img alt=\\\"")) != -1){
            text = text.substring(index+11);
            String name = text.substring(0, text.indexOf("\\\""));
            if(outputWhenLoading) System.out.println(name);
            text = text.substring(text.indexOf("<\\/a>：")+6);
            String info = text.substring(0, text.indexOf("<\\/div>"));
            while (!info.isEmpty() && info.endsWith(" ")){
                info = info.substring(0, info.length()-1);
            }

            //将emoji从html标签抽离出来
            info = DanMuConverter.replaceEmoji(info);

            if(outputWhenLoading) System.out.println(info);

            text = text.substring(text.indexOf("<div class=\\\"WB_from S_txt2\\\">")+30);
            String time = text.substring(0, text.indexOf("<\\/div>"));
            if(time.contains("月")){
                time = calendar.get(Calendar.YEAR)+"-"+time.replace("月", "-").replace("日", "");
            }
            time += ":00";
            if(outputWhenLoading) System.out.println(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = simpleDateFormat.parse(time);
            long ts = date.getTime();
            if(outputWhenLoading) System.out.println(String.valueOf(ts));

            danMuList.add(new DanMu(name, info, ts));

            if(outputWhenLoading) System.out.println(" ");
        }

        if(text2.contains("root_comment_max_id")) {
            text2 = text2.substring(text2.indexOf("root_comment_max_id=") + 20);
            String next2 = text2.substring(0, text2.indexOf("&"));
            String nextURL = "https://weibo.com/aj/v6/comment/big?ajwvr=6&id="+id+
                    "&root_comment_max_id="+next2+
                    "&root_comment_max_id_type=&root_comment_ext_param=&page="+page * 15+
                    "&filter=all&sum_comment_number="+(page+1)+"&filter_tips_before=0&from=singleWeiBo";

            if(outputWhenLoading) System.out.println("next=" + next2);

            String nextOrigin = StringUtil.unicodeToCn(HttpUtil.sendGET(nextURL));

            next(nextOrigin, danMuList, id, page+1);
        } else {
            if(outputWhenLoading) System.out.println("end");
        }
    }

}
