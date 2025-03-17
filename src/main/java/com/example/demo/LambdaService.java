package com.example.demo;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * 业务客户端调用Lambda样例代码
 * */
@Service
public class LambdaService {

    private final String AWS_URL = "http://169.254.169.254/latest/meta-data/local-ipv4";

    private final String RSA = "RSA";

    private final LambdaClient lambdaClient;

    public LambdaService() {
        // 替换为您的区域
        this.lambdaClient = LambdaClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    /**
     * 请求lambda主入口
     * */
    public String sendRequest(LambdaEnum lambdaEnum) {
        // 构造加密请求的payload
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", getMetadata(AWS_URL));
        jsonObject.put("scene", lambdaEnum.getScene());
        return invokeLambda(lambdaEnum.getFunctionName(), jsonObject.toJSONString());
    }

    /**
     * BC专用解密方法
     * */
    public String decryptForBc(String res, String privateKey) throws Exception {
        List<String> list = JSONObject.parseObject(res, List.class);

        PrivateKey privateKeyFromString = getPrivateKeyFromString(privateKey);

        List<String> decryptedChunks = decryptPrivateKeyChunks(list, privateKeyFromString);
        StringBuilder decryptedContent = new StringBuilder();
        for (var decryptedChunk : decryptedChunks) {
            decryptedContent.append(decryptedChunk);
        }
        return decryptedContent.toString();
    }

    // 请求lambda
    private String invokeLambda(String functionName, String payload) {
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String(payload))
                .build();
        return lambdaClient.invoke(invokeRequest).payload().asUtf8String();
    }

    // 从字符串中加载私钥
    private PrivateKey getPrivateKeyFromString(String privateKeyStr) throws Exception {
        String privateKeyPEM = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }

    // 解密方法
    private String decryptWithPrivateKey(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // 解密
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    // 解密 List<String> 中的每个加密段
    private List<String> decryptPrivateKeyChunks(List<String> encryptedChunks, PrivateKey privateKey) throws Exception {
        List<String> decryptedChunks = new ArrayList<>();
        for (String encryptedChunk : encryptedChunks) {
            String decrypted = decryptWithPrivateKey(encryptedChunk, privateKey);
            decryptedChunks.add(decrypted);
        }
        return decryptedChunks;
    }

    private static String getMetadata(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = reader.readLine();
            reader.close();
            return result != null ? result : "";
        } catch (Exception e) {
            return "Fetch fail";
        }
    }
}