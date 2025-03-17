package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ip", new JsonPrimitive(getMetadata(AWS_URL)));
        jsonObject.add("scene", new JsonPrimitive(lambdaEnum.getScene()));
        return invokeLambda(lambdaEnum.getFunctionName(), jsonObject.toString());
    }

    /**
     * BC专用解密方法
     * */
    public String decryptForBc(String res, String privateKey) throws Exception {
        System.out.println("|||"+ res +"|||");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>(){}.getType();
        List<String> list = gson.fromJson(res, listType);
        System.out.println("-------|||" + privateKey + "|||---------");
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

    public static void main(String[] args) {
        String res = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDchE8PqKH1El6B\n" +
                "YF0C2oDdXy0R4FdupQ0jODbWQie8eP7PVS8Z/uIX9HyxLEtLoi5vA 2cKydF2V0U\n" +
                "94IHEPol4j85j2e3hCmsu2f5dk7VGJC CyPGaw7TE3O/3fYnuopbxCFMscEB0SAZ\n" +
                "L0bQ2LjXARf7cbCttw1BfsNdUS1PpV/HRAbcr/hTUrnZiyA/45yKRqpwmwohJvNO\n" +
                "67latDl01wrm8AhW/lNu fCc4lz76yLqsp9Hs0WxqMCWh2cdo0ksIVZ/rkBHEs8O\n" +
                "ZD mDvjk0cOy3BTMrSVsIZlp/vyB/l09dtC66c8w0uVuobKkRKujDZmdnH/qZpm2\n" +
                "/spq3 EvAgMBAAECggEBAIB6GbIO1uV5IVSRdz9dXO0dMZ4Trf0J7sCRVOm6O3qE\n" +
                "qDjV8Y5cwQ h4zA6lVfoktMdX8e4ILaCcMfxeHFzg7qkuZ9onM4AE2PPAEIImt9u\n" +
                "443F4b8NyuJRmAO2Z184QLo8aAZOHhxX4jKqJ8EwM2kY wcFcyGS4AdDusFYtWSL\n" +
                "6YLD QHMGmj6oLRIVu9l2c7Wau07KrJzxpss JPPonKG 1 jjZsdgGn05oJ64NEj\n" +
                "zDkEVdSTmI05ITTdyXxfTPJ/wT/ aYHRET776quqowRHbj0nDbB9sXcsSr8bVXnv\n" +
                "vGmIisWfMSbKmMwB1r8Ik0qAbrqLQ71BTC/o5akXOwkCgYEA8JrqjypxRvhIikVx\n" +
                "ylwL51xmRA6/4aSzZxzSlo2kjfWfh/GiI2XPg vIdQAK6 97qr7WgsoOSpQvAJ2b\n" +
                "szFRSNRAdYOSFXyZBSLQA6RM0Ab/0BpFVCu1UdwUTOjAm8mW89aGGaTMlw yxk2H\n" +
                "Up4yk/T0wNfOsfNR85TLlM51R8sCgYEA6qBZM26aFn/ utpSehpitj2Y5Acn/ ET\n" +
                "rBARlpjKhHSKUfbAwU2M/9RBhhNvkpkZfN9Zs7glQh/RTvlL1JK/uB HVaHgLZr0\n" +
                "6t9t5mtYrdM3m/avckaU1zX8DiZbLvGjqci9bFzvQR0VlbVuKd8zxG3ZYvlLgZ l\n" +
                "dSmB51Xdd60CgYApG36puSmjY3YZUaYQWcua2rmKNS7pYVdZbZ45JLgRnP6Fnm4C\n" +
                "ODnNIzKbcsdq6f6p/HLv44 vhEPHiiX45pspo0HkEfeafjAypXD vkp1XEzGhEaG\n" +
                "C/Wtp7k8LqQBud51iJhetz5RLNhxcvuEzQx9JOPQMs7Yrma5BBoIMYHuIwKBgH 5\n" +
                "zPWHx1BqW2SjB3U7OfOVss6n6qmMgOYRVVHTWaQjwUz/dE2HBzQZ 5WerQV0XQCy\n" +
                "oiWJIJdPtOU2J4bQYJg abE/T0fkbGIUQcLHO15ddo9sCnS YbaKx 14CfmTSBJc\n" +
                "mmoMaZ7b021NeGxI uh7GHWd2vH00 DlOLeT8d19AoGAFMkvdeEePusrUz ORhRI\n" +
                "q8MuO6MMLip JbkUa9DOnhWd0369HpAzEV9NGY9GVlFu8CyS60z9fpjxQ7joUvxO\n" +
                "kxzlRVSEpJw8 iY/Y1dNVEw2dqrzlfgW4mLTk517dyK4YeOUMUh/lmdVFk59cQKj\n" +
                "Z5FAPr0FlPuwVNpTtb2p2nw=";
        String privateKeyPEM = res.replaceAll("\n", "").replaceAll(" ","");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            keyFactory.generatePrivate(keySpec);
            System.out.println("succ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从字符串中加载私钥
    private PrivateKey getPrivateKeyFromString(String privateKeyStr) throws Exception {
        String privateKeyPEM = privateKeyStr.replaceAll("\n", "").replaceAll(" ","");

        System.out.println(privateKeyPEM);
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