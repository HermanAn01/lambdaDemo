package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private LambdaService lambdaService;

    @PostMapping("test")
    public void demo(@RequestBody Bean bean){

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
}
