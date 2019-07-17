package com.jraft.Message;

import lombok.Data;

/**
 * @author chenchang 消息基类
 * @date 2019/7/15 21:49
 */
@Data
public class Message {
    /**
     * 请求上下文，用来绑定请求和对应的响应
     */
    private Long context;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 接受方
     */
    private Integer to;
    /**
     * 发送方
     */
    private Integer from;
    /**
     * 任期
     */
    private Integer Term;
    /**
     * 领导人的 Id，以便于跟随者重定向请求
     */
    private Integer leaderId;
    /**
     * 新的日志条目紧随之前的索引值
     */
    private Integer prevLogIndex;
    /**
     * prevLogIndex 条目的任期号
     */
    private Integer prevLogTerm;
    /**
     * 最新的索引
     */
    private Integer index;
    /**
     * 已提交的索引
     */
    private Integer commit;
    /**
     * 数据
     */
    private Entry[] entries;
}



