package com.jraft.Message;

import lombok.Data;

/**
 * @author chenchang 投票
 * @date 2019/7/15 21:57
 */
public interface MsgVote {
    @Data
    class MsgVoteReq extends Message {
        //候选人的任期号
        private int term;
        //请求选票的候选人的 Id
        private int candidateId;
        //候选人的最后日志条目的索引值
        private int lastLogIndex;
        //候选人最后日志条目的任期号
        private int lastLogTerm;
    }

    @Data
    class MsgVoteResp extends Message {
        //当前任期号，以便于候选人去更新自己的任期号
        private int term;
        //候选人赢得了此张选票时为真
        private boolean voteGranted;
    }

}
