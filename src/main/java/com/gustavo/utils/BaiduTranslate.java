package com.gustavo.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.gustavo.setting.AppSettingsState;

public class BaiduTranslate {
    private static final String APP_ID = ""; // 替换成你的百度翻译 API Key
    private static final String SECRET_KEY = ""; // 替换成你的百度翻译 Secret Key
    private static final String from = "en"; // 源语言，这里是英文
    private static final String to = "zh"; // 目标语言，这里是中文

    public static String translate(String query) throws IOException, NoSuchAlgorithmException {
        AppSettingsState appSettingsState = AppSettingsState.getInstance();

        String appid = appSettingsState.getState().appid;
        String secretKey = appSettingsState.getState().secretKey;
        if (Strings.isNullOrEmpty(appid)) {
            appid = APP_ID;
        }
        if (Strings.isNullOrEmpty(secretKey)) {
            secretKey = SECRET_KEY;
        }

        String apiUrl = "http://api.fanyi.baidu.com/api/trans/vip/translate";
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = generateSign(appid, query, salt, secretKey);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlStr = String.format("%s?q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s",
                apiUrl, encodedQuery, from, to, appid, salt, sign);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            System.out.println(response.toString());

            return parseTranslationResponse(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query;
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
