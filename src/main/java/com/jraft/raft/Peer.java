package com.jraft.raft;

import lombok.Data;

import java.net.InetSocketAddress;

/**
 * @author chenchang
 * @date 2019/6/25 21:05
 */
@Data
public class Peer {
    private int id;
    //对于每一个服务器，已经复制给他的日志的最高索引值
    private int matchIndex;
    //对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一）
    private int nextIndex;

    //是否是存活的
    private boolean isActiv;

    public Peer(int id, int matchIndex, int nextIndex) {
        this.id = id;
        this.matchIndex = matchIndex;
        this.nextIndex = nextIndex;
    }
}
