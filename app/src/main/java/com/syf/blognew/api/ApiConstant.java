package com.syf.blognew.api;

public class ApiConstant {
    // 1. 基础域名（统一在这里改）
    public static final String BASE_URL = "https://api.yfsun2.com";
//    public static final String BASE_URL = "http://192.168.10.17:8091";

    // 2. 所有接口统一写这里
    public static final String USER_LOGIN      = BASE_URL + "/login";
    public static final String USER_REGISTER   = BASE_URL + "/user/add";
    public static final String USER_BY_NAME   = BASE_URL + "/user/getUserByName/";
    public static final String USER_LIST   = BASE_URL + "/user/queryListByName/";
    public static final String UPDATE_PAY_PWD   = BASE_URL + "/user/updatePayPwd";
    public static final String UPDATE_PWD   = BASE_URL + "/user/updatePwd";
    public static final String UPDATE_AVATAR   = BASE_URL + "/user/updateAvatar/";
    public static final String UPDATE_USERNAME   = BASE_URL + "/user/updateUserName/";
    public static final String UPDATE_SCORE   = BASE_URL + "/user/updateScore";
    public static final String TRANSFER   = BASE_URL + "/message/transfer";
    public static final String RECEIVE   = BASE_URL + "/message/receive/";
    public static final String FRIEND_LIST   = BASE_URL + "/friend/queryList";
    public static final String FRIEND_ADD   = BASE_URL + "/friend/add/";
    public static final String FRIEND_AGREE   = BASE_URL + "/friend/agree/";
    public static final String FRIEND_REFUSE   = BASE_URL + "/friend/refuse/";
    public static final String SEND_MAIL   = BASE_URL + "/sendMail";
    public static final String BLOG_ADD        = BASE_URL + "/blog/add";
    public static final String BLOG_PAGE       = BASE_URL + "/blog/page/";
    public static final String BLOG_DELETE      = BASE_URL + "/blog/delete/";
    public static final String BLOG_PRIVATE      = BASE_URL + "/blog/setPrivate/";
    public static final String BLOG_PUBLIC     = BASE_URL + "/blog/setPublic/";
    public static final String COMMENT_ADD     = BASE_URL + "/comment/add";

    public static final String MESSAGE_ADD=BASE_URL+"/message/add";
    public static final String MESSAGE_PAGE=BASE_URL+"/message/page/";
    public static final String MESSAGE_WITHDRAW=BASE_URL+"/message/withdraw/";

    public static final String SUPPORT_ADD=BASE_URL+"/support/add/";
    public static final String SUPPORT_DELETE=BASE_URL+"/support/delete/";

    public static final String FILE_UPLOAD="http://192.168.10.17:8083/file/upload";

    public static final String USER_INFO=BASE_URL+"/user/userinfo";

    public static final String SIGN_CHECK=BASE_URL+"/user/signCheck";

    public static final String SIGN=BASE_URL+"/user/sign";

    public static final String GIFT_LIST=BASE_URL+"/gift/list";
    public static final String GIFT_ADD=BASE_URL+"/gift/add";
    public static final String GIFT_UPDATE=BASE_URL+"/gift/update";
    public static final String GIFT_DELETE=BASE_URL+"/gift/delete/";
    public static final String GIFT_EXCHANGE=BASE_URL+"/gift/exchange/";

    public static final String NOTICE_LIST=BASE_URL+"/notice/list";
    public static final String NOTICE_LIST_ALL=BASE_URL+"/notice/queryList";
    public static final String NOTICE_SET_READ=BASE_URL+"/notice/setRead/";
    public static final String NOTICE_UNREAD=BASE_URL+"/notice/unread";
    public static final String NOTICE_GOODS_SEND=BASE_URL+"/notice/goodsSend/";
    public static final String NOTICE_GOODS_FINISH=BASE_URL+"/notice/goodsFinish/";
    public static final String NOTICE_GOODS_REFUSE=BASE_URL+"/notice/goodsRefuse/";

    public static final String GET_RECEIVE_QRCODE=BASE_URL+"/pointcode/getReceiveQr/";
    public static final String GET_PAY_QRCODE=BASE_URL+"/pointcode/getPayQr";
    public static final String PAY_BY_RECEIVE=BASE_URL+"/pointcode/payByReceive";
    public static final String PAY_BY_PAY_CODE=BASE_URL+"/pointcode/payByPayCode";
    public static final String NFC_HONGBAO=BASE_URL+"/pointcode/nfcHongbao";
    public static final String GET_HONGBAO=BASE_URL+"/pointcode/getHongbao/";

}
