package com.example.demo;

public enum LambdaEnum {

    LB_SM("BC", "secrets"),
    ;

    private String scene;
    private String functionName;

    LambdaEnum(String scene, String functionName){
        this.scene = scene;
        this.functionName = functionName;
    }

    public String getScene() {
        return scene;
    }

    public String getFunctionName() {
        return functionName;
    }
}
