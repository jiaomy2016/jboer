package com.dcits.jb.baidu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;

import com.dcits.jb.baidu.json.JSONObject;

public class Asr {

    private static final String serverURL = "http://vop.baidu.com/server_api";
    private static String token = "";
   // private static final String testFileName = "d:\\temp\\test.pcm";
    //put your own params here
    private static final String apiKey = "ROymsLbbkB6PlPXTjbnY70Kx";
    private static final String secretKey = "sxnalkP0ZXSKFCj9aMyknVqYm67aCAyk";
    private static final String cuid = "7541281";

    static  {
        try {
			getToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
  public static void main(String[] args) throws Exception {
	String tt=method1("qq");
	
	System.out.println(tt);
	
}
    /**
     * @author lips
     * 获取token
     * 
     * @throws Exception
     */
    private static void getToken() throws Exception {
        String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" + 
            "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        token = new JSONObject(printResponse(conn)).getString("access_token");
    }
    
    /**
     * 传入pcm文件，然后转化为文本，并提取文本内容
     * @author lips
     * @param testFileName
     * @throws Exception
     */
    public static String method1(String testFileName) throws Exception {
    	String resultText="";
    	testFileName="d:\\temp\\test.pcm";
    	File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();

        // construct params
        JSONObject params = new JSONObject();
        params.put("format", "pcm");
        params.put("rate", 8000);
        params.put("channel", "1");
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("len", pcmFile.length());
        params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();
        
        //提取转化结果中的文本
        String response=printResponse(conn);
        JSONObject jsonobj =new JSONObject(response);
        com.dcits.jb.baidu.json.JSONArray array=  jsonobj.getJSONArray("result");
        String js="";
         int len= array.length();
         for (int i = 0; i < len; i++) {
			js= array.getString(i);
			if (js!="") {
				resultText+=js.toString();
				resultText+=",";
			}
		}
        return resultText;
    }
   
    //语音转化的第二种方法
    private static void method2(String testFileName) throws Exception {
    	testFileName="d:\\temp\\test.pcm";
    	File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL
                + "?cuid=" + cuid + "&token=" + token).openConnection();

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "audio/pcm; rate=8000");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(loadFile(pcmFile));
        wr.flush();
        wr.close();

        printResponse(conn);
    }
    
    //处理语音转化结果
    private static String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            // request error
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }
    //加载要转化的pcm文件
    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

}
