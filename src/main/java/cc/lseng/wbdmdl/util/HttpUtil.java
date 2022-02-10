package cc.lseng.wbdmdl.util;

import cc.lseng.wbdmdl.Config;
import cc.lseng.wbdmdl.Main;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author LSeng
 * @date 2022/2/8 16:15
 */
public class HttpUtil {

    public static String sendGET(String urlStr) throws IOException {
        HttpClient httpClient = new HttpClient();
        //超时时间为10s
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        //GET请求实例对象
        GetMethod getMethod = new GetMethod(urlStr);
        //设置get请求超时时间为10s
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT,10000);
        //设置请求头
        getMethod.addRequestHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        getMethod.addRequestHeader("accept-language", "zh-CN,zh;q=0.9");
        getMethod.addRequestHeader("cache-control", "no-cache");
        getMethod.addRequestHeader("cookie", Config.cookie);
        getMethod.addRequestHeader("pragma", "no-cache");
        getMethod.addRequestHeader("sec-ch-ua", "\"Chromium\";v=\"88\", \"Google Chrome\";v=\"88\", \";Not A Brand\";v=\"99\"");
        getMethod.addRequestHeader("sec-ch-ua-mobile", "?0");
        getMethod.addRequestHeader("Upgrade-Insecure-Requests", "1");
        getMethod.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36");

        httpClient.executeMethod(getMethod);

//        String result = getMethod.getResponseBodyAsString();
        InputStream inputStream = getMethod.getResponseBodyAsStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer stringBuffer = new StringBuffer();
        String str= "";
        while((str = br.readLine()) != null){
            stringBuffer .append(str );
        }
        String result = stringBuffer.toString();

        //释放http连接
        getMethod.releaseConnection();

        return result;
    }

    static {
        DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
    }
}
