package cc.lseng.wbdmdl.util;

import cc.lseng.wbdmdl.DanMu;
import cc.lseng.wbdmdl.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DanMuConverter {

    private static Calendar calendar = Calendar.getInstance();

    public static long getTimeStamp(String start) throws Exception{
        if(start.endsWith(" ")){
            start = start.substring(0, start.length()-1);
        }
        if(start.contains("月")){
            start = calendar.get(Calendar.YEAR)+"-"+start.replace("月", "-").replace("日", "");
        } else if (start.matches("[0-9]{1,2}.[0-9]{1,2}.\\s[0-9]{1,2}:[0-9]{1,2}")){
            //防止乱码而识别不到“月”
            start = calendar.get(Calendar.YEAR)+"-"+start.replaceFirst("[^0-9\\s]", "-").replaceFirst("[^0-9\\s\\-]", "");
        }
        start += ":00";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = simpleDateFormat.parse(start);

        return date.getTime();
    }

    public static void convertToXML(String filePath, List<DanMu> list, long start) {
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }

        writeIntoFile(filePath, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"#s\"?>\n" +
                "<i>\n");

        for (int i = list.size() - 1; i >= 0; i--) {
            DanMu danMu = list.get(i);
            String second = String.valueOf((danMu.time - start) / 1000);
            String result = "  <d p=\"" + second + ",1,25,16777215," + danMu.time + ",0,1602022773,0\" user=\"" + danMu.name + "\">" + danMu.info + "</d>\n";
            writeIntoFile(filePath, result);
        }

        writeIntoFile(filePath, "</i>");
    }

    public static void convertToJSON(String filePath, List<DanMu> list, long start) {
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }

        JSONArray array = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            JSONObject obj = new JSONObject();
            DanMu danMu = list.get(i);
            obj.put("start", (danMu.time - start) / 1000);
            obj.put("style", "scroll");
            obj.put("color", "ffffff");
            obj.put("commenter", danMu.name);
            obj.put("content", danMu.info);
            obj.put("is_guest", false);
            array.put(obj);
        }

        writeIntoFile(filePath, array.toString(Main.indent ? 4 : 0));
    }

    public static void resetTime(List<DanMu> list, long startTimeStamp){
        //将所有时间段的弹幕(分钟)分开
        Map<Long, List<DanMu>> map = new HashMap<>();
        for(DanMu danMu : list){
            if(map.containsKey(danMu.time)){
                map.get(danMu.time).add(danMu);
            } else {
                List<DanMu> l = new ArrayList<>();
                l.add(danMu);
                map.put(danMu.time, l);
            }
        }
        //将所有时间段的弹幕平均分配到各个分钟中
        for(List<DanMu> l : map.values()){
            //每个弹幕相隔的时间，最短1ms
            long tick = Math.max(1, 60_000 / l.size());
            for (int i = 0; i < l.size(); i++) {
                l.get(i).time += tick * i;
            }
        }
        //将所有弹幕提前一分钟，但不早于开播时间
        for(DanMu danMu : list){
            danMu.time = Math.max(startTimeStamp, danMu.time - 60_000);
        }
    }

    public static String replaceEmoji(String content){
        while (content.contains("<img") || content.contains("<a")){
            int indexImg = content.indexOf("<img");
            int indexA = content.indexOf("<a");
            if(indexImg != -1 && indexA == -1){
                //只包含Emoji
                content = replaceFrontImg(content);
            } else if (indexImg == -1 && indexA != -1){
                //只包含@
                content = replaceFrontAt(content);
            } else {
                //既包含Emoji又包含@
                if(indexImg < indexA){
                    //Emoji在前面
                    content = replaceFrontImg(content);
                } else {
                    //@在前面
                    content = replaceFrontAt(content);
                }
            }
        }
        return content;
    }

    private static String replaceFrontImg(String content){
        String bad = content.substring(content.indexOf("<img"), content.indexOf("/>")+2);
        if(bad.contains("title=\"")){
            String sec = bad.substring(bad.indexOf("title=\"")+7);
            String emojiStr = sec.substring(0, sec.indexOf("\""));
            content = content.replace(bad, emojiStr);
        } else if(bad.contains("title=\\\"")){
            String sec = bad.substring(bad.indexOf("title=\\\"")+8);
            System.out.println(sec);
            String emojiStr = sec.substring(0, sec.indexOf("\\\""));
            content = content.replace(bad, emojiStr);
        } else {
            content = content.replace(bad, "");
        }
        return content;
    }

    private static String replaceFrontAt(String content){
        while (content.contains("<a")) {
            String bad = content.substring(content.indexOf("<a"), content.indexOf("</a>") + 4);
            if(bad.contains("@")) {
                String at = bad.substring(bad.indexOf(">") + 1, bad.indexOf("</a>"));
                content = content.replace(bad, at);
            } else {
                content = content.replace(bad, "");
            }
        }
        return content;
    }

    public static void writeIntoFile(String filePath, String info){
        String filePath2 = filePath.replace("\\", "/");
        int index = filePath2.lastIndexOf("/");
        String dir = filePath2.substring(0, index);
        File fileDir = new File(dir);
        fileDir.mkdirs();
        File file = new File(filePath);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            try(FileWriter fileWriter = new FileWriter(file, true)) {
                fileWriter.write(info);
                fileWriter.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
