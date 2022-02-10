package cc.lseng.wbdmdl;

import cc.lseng.wbdmdl.util.DanMuConverter;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author LSeng
 * @date 2022/2/10 12:54
 */
public class Config {

    public static String cookie;
    public static int frequency = 10;
    public static int wait = 3000;

    public static void readConfig() throws IOException {
        String fileName = "./config.json";

        File f = new File(fileName);
        System.out.println("正在检查配置文件："+f.getAbsolutePath());
        if(!f.exists()){
            try {
                f.createNewFile();
                DanMuConverter.writeIntoFile(fileName,
                        new JSONObject(
                                "{\"cookie\":\"cookie\"," +
                                        "\"frequency\":10," +
                                        "\"wait\":3000," +
                                "}").toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Stream<String> lines = Files.lines(Paths.get(fileName));

        StringBuilder sb = new StringBuilder();
        lines.forEach(sb::append);

        JSONObject json = new JSONObject(sb.toString());

        frequency = json.getInt("frequency");
        wait= json.getInt("wait");
        cookie= json.getString("cookie");

        if(frequency <= 1){
            frequency = 2;
        }
        if(wait < 0){
            wait = 0;
        }
    }

}
