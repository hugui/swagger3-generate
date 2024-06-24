package com.gustavo.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.*;

public class BaiduTranslate {
    private static final String apiKey = "20210610000859390"; // 替换成你的百度翻译 API Key
    private static final String secretKey = "JKSSJF5D4EMBZNw4p2Yf"; // 替换成你的百度翻译 Secret Key
    private static final String from = "en"; // 源语言，这里是英文
    private static final String to = "zh"; // 目标语言，这里是中文

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String query = "content Title"; // 要翻译的文本
        String translatedText = translate(query);
        System.out.println("翻译结果：" + translatedText);
    }

    public static String translate(String query) throws IOException, NoSuchAlgorithmException {
        String apiUrl = "http://api.fanyi.baidu.com/api/trans/vip/translate";
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = generateSign(apiKey, query, salt, secretKey);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlStr = String.format("%s?q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s",
                apiUrl, encodedQuery, from, to, apiKey, salt, sign);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return parseTranslationResponse(response.toString());
        }
    }

    private static String generateSign(String apiKey, String query, String salt, String secretKey) throws NoSuchAlgorithmException {
        String sign = apiKey + query + salt + secretKey;
        return md5(sign);
    }

    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private static String parseTranslationResponse(String jsonResponse) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        JsonArray transResult = jsonObject.getAsJsonArray("trans_result");
        JsonObject firstResult = transResult.get(0).getAsJsonObject();
        return firstResult.get("dst").getAsString();
    }

}
