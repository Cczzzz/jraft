package com.jraft.Message;

/**
 * @author chenchang 所有的消息类型
 * @date 2019/7/15 21:51
 */
public interface MsgType {
    Integer MsgHup = 0; //选举计时器超时
    Integer MsgBeat = 1; //本地消息  向其它节点探活 会转化为MsgHeartbeat 事件
    Integer MsgProp = 2;
    Integer MsgApp = 3; // leader 广播消息
    Integer MsgAppResp = 4;
    Integer MsgVote = 5; // 选举投票
    Integer MsgVoteResp = 6; // 选举投票响应
    Integer MsgSnap = 7;
    Integer MsgHeartbeat = 8; //心跳事件
    Integer MsgHeartbeatResp = 9;//心跳响应
    Integer MsgUnreachable = 10;
    Integer MsgSnapStatus = 11;
    Integer MsgCheckQuorum = 12; //本地消息   检查能否于集群中其它节点通信 在etcd中不向网络层发送消息，仅查询本地存储的其它节点是否存活
    Integer MsgTransferLeader = 13;
    Integer MsgTimeoutNow = 14;
    Integer MsgReadIndex = 15;
    Integer MsgReadIndexResp = 16;
    Integer MsgPreVote = 17; //pre 选举投票
    Integer MsgPreVoteResp = 18;//pre 选举投票响应
}
