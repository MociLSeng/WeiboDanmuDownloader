package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.DanMuConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    public static final String VERSION = "v1.0";
    public static final boolean indent = true;

    public static void main(String[] args) throws IOException {
        System.out.println("WeiboDanmuDownloader "+VERSION+" by LSeng");
        try {
            Config.readConfig();
            if(!CookieChecker.check()){
                System.out.println("cookie设置错误，请检查config.json");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br =new BufferedReader(new InputStreamReader(System.in));

        System.out.println("请输入new/old/one");

        String operation = br.readLine().toLowerCase();
        switch (operation){
            case "new":{
                System.out.println("请输入mid");
                String mid = br.readLine();
                System.out.println("请输入直播开始的时间(示例：2020-5-9 17:21，不会影响渲染结果)");
                String time = br.readLine();
                br.close();
                System.out.println("正在获取...");
                try {
                    NewDanmuDownloader.get("./download/"+mid+".xml",
                            mid, DanMuConverter.getTimeStamp(time));
                } catch (Exception e) {
                    System.out.println("获取失败");
                    e.printStackTrace();
                }
                break;
            }
            case "old":{
                System.out.println("请输入id");
                String id = br.readLine();
                System.out.println("请输入直播开始的时间(示例：2020-5-9 17:21，注意会影响渲染结果)");
                String time = br.readLine();
                br.close();
                System.out.println("正在获取...");
                try {
                    OldDanmuDownloader.get("./download/"+id+".xml",id, time);
                } catch (Exception e) {
                    System.out.println("获取失败");
                    e.printStackTrace();
                }
                break;
            }
            case "one":{
                System.out.println("请输入uid");
                String uid = br.readLine();
                System.out.println("请输入直播开始的时间(示例：2020-5-9 17:21，不会影响渲染结果)");
                String time = br.readLine();
                br.close();
                System.out.println("正在获取...");
                try {
                    OneLiveDanmuDownloader.get("./download/"+uid+".xml",
                            uid, DanMuConverter.getTimeStamp(time));
                } catch (Exception e) {
                    System.out.println("获取失败");
                    e.printStackTrace();
                }
                break;
            }
            default:{
                System.out.println("输入的内容有误");
                br.close();
                return;
            }
        }
    }

}
