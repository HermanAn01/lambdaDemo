package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller("demo")
public class DemoController {

    @Resource
    private LambdaService lambdaService;

    @RequestMapping("test")
    public void demo(String publickey){

        String res = lambdaService.sendRequest(LambdaEnum.LB_SM);
        System.out.println(res);

        try {
            String ret = lambdaService.decryptForBc(res, publickey);
            System.out.println(ret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("demo");
    }
}
