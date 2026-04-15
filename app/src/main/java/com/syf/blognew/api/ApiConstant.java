package com.syf.blognew.api;

public class ApiConstant {
    // 1. 基础域名（统一在这里改）
//    public static final String BASE_URL = "https://api.yfsun2.com";
    public static final String BASE_URL = "http://192.168.10.17:8091";

    // 2. 所有接口统一写这里
    public static final String USER_LOGIN      = BASE_URL + "/login";
    public static final String USER_REGISTER   = BASE_URL + "/user/add";
    public static final String USER_BY_NAME   = BASE_URL + "/user/getUserByName/";
    public static final String USER_LIST   = BASE_URL + "/user/queryList/";
    public static final String FRIEND_LIST   = BASE_URL + "/friend/queryList";
    public static final String SEND_MAIL   = BASE_URL + "/sendMail";
    public static final String BLOG_ADD        = BASE_URL + "/blog/add";
    public static final String BLOG_PAGE       = BASE_URL + "/blog/page/";
    public static final String BLOG_DELETE      = BASE_URL + "/blog/delete/";
    public static final String COMMENT_ADD     = BASE_URL + "/comment/add";

    public static final String MESSAGE_ADD=BASE_URL+"/message/add";
    public static final String MESSAGE_PAGE=BASE_URL+"/message/page/";

    public static final String SUPPORT_ADD=BASE_URL+"/support/add/";
    public static final String SUPPORT_DELETE=BASE_URL+"/support/delete/";

    public static final String FILE_UPLOAD="http://192.168.10.17:8083/file/upload";

}
