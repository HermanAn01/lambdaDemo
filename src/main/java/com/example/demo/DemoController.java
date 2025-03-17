package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private LambdaService lambdaService;

    @PostMapping("test")
    public void demo(@RequestBody Bean bean){
        String publicIp = getPublicIp();
        String privateIp = getPrivateIp();

        System.out.println("Public IP: " + publicIp);
        System.out.println("Private IP: " + privateIp);


        String res = lambdaService.sendRequest(LambdaEnum.LB_SM);
        System.out.println(res);
        if("".equals(res)){
            System.out.println("无效返回值");
            return;
        }
        try {
            String ret = lambdaService.decryptForBc(res, bean.getPrivateKey());
            System.out.println(ret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("demo");
    }


    // 获取公共 IP 地址
    public static String getPublicIp() {
        return getMetadata("http://169.254.169.254/latest/meta-data/public-ipv4");
    }

    // 获取私有 IP 地址
    public static String getPrivateIp() {
        return getMetadata("http://169.254.169.254/latest/meta-data/local-ipv4");
    }

    // 获取指定元数据
    private static String getMetadata(String metadataUrl) {
        try {
            URL url = new URL(metadataUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ip = reader.readLine();
            reader.close();

            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
