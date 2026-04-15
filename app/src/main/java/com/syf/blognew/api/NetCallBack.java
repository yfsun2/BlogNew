package com.syf.blognew.api;

public interface NetCallBack {

    void onFailure(int code,String msg);
    void onSuccess(String json);
}
