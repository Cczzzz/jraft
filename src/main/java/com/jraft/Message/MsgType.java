package com.jraft.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenchang 所有的消息类型
 * @date 2019/7/15 21:51
 */
public interface MsgType {
    Map<Integer, String> MAP = new HashMap<Integer, String>() {{
        put(MsgHup, "MsgHup");
        put(MsgBeat, "MsgBeat");
        put(MsgProp, "MsgProp");
        put(MsgApp, "MsgApp");
        put(MsgAppResp, "MsgAppResp");
        put(MsgSnap, "MsgSnap");
        put(MsgVote, "MsgVote");
        put(MsgVoteResp, "MsgVoteResp");
        put(MsgHeartbeat, "MsgHeartbeat");
        put(MsgHeartbeatResp, "MsgHeartbeatResp");
        put(MsgUnreachable, "MsgUnreachable");
        put(MsgSnapStatus, "MsgSnapStatus");
        put(MsgCheckQuorum, "MsgCheckQuorum");
        put(MsgTransferLeader, "MsgTransferLeader");
        put(MsgTimeoutNow, "MsgTimeoutNow");
        put(MsgReadIndex, "MsgReadIndex");
        put(MsgReadIndexResp, "MsgReadIndexResp");
        put(MsgPreVote, "MsgPreVoteResp");
        put(MsgPreVoteResp, "MsgPreVoteResp");
    }};

    static String TypeDescription(int type) {
        return MAP.get(type);
    }

    int MsgHup = 0; //选举计时器超时
    int MsgBeat = 1; //本地消息  向其它节点探活 会转化为MsgHeartbeat 事件
    int MsgProp = 2;
    int MsgApp = 3; // leader 广播消息
    int MsgAppResp = 4;
    int MsgVote = 5; // 选举投票
    int MsgVoteResp = 6; // 选举投票响应
    int MsgSnap = 7;
    int MsgHeartbeat = 8; //心跳事件
    int MsgHeartbeatResp = 9;//心跳响应
    int MsgUnreachable = 10;
    int MsgSnapStatus = 11;
    int MsgCheckQuorum = 12; //本地消息   检查能否于集群中其它节点通信 在etcd中不向网络层发送消息，仅查询本地存储的其它节点是否存活
    int MsgTransferLeader = 13;
    int MsgTimeoutNow = 14;
    int MsgReadIndex = 15;
    int MsgReadIndexResp = 16;
    int MsgPreVote = 17; //pre 选举投票
    int MsgPreVoteResp = 18;//pre 选举投票响应
}
