package com.syf.blognew.pojo.req;

import lombok.Data;

/**
 * @author yfsun10
 * @version 1.0
 * @date 2021/6/7 14:29
 */
@Data
public class CommentAddReq {
    private Integer blogId;
    private String content;
    private Integer fromUid;
    private Integer toUid;

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getFromUid() {
        return fromUid;
    }

    public void setFromUid(Integer fromUid) {
        this.fromUid = fromUid;
    }

    public Integer getToUid() {
        return toUid;
    }

    public void setToUid(Integer toUid) {
        this.toUid = toUid;
    }
}
