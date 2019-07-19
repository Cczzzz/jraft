package com.jraft.Message;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenchang 投票
 * @date 2019/7/15 21:57
 */
public interface MsgVote {
    @Getter
    @Setter
    class MsgVoteReq extends Message {
        //候选人的任期号
        //private Integer term;
        //请求选票的候选人的 Id
        private Integer candidateId;
        //候选人的最后日志条目的索引值
        // private Integer lastLogIndex;
        //候选人最后日志条目的任期号
        private Integer lastLogTerm;

        @Override
        public String toString() {
            return "MsgVoteReq{" +
                    "candidateId=" + candidateId +
                    ", lastLogTerm=" + lastLogTerm +
                    "} " + super.toString();
        }
    }

    @Getter
    @Setter
    class MsgVoteResp extends Message {
        //当前任期号，以便于候选人去更新自己的任期号
        //private Integer term;
        //候选人赢得了此张选票时为真
        private Boolean voteGranted;

        @Override
        public String toString() {
            return "MsgVoteResp{" +
                    "voteGranted=" + voteGranted +
                    "} " + super.toString();
        }
    }

    static Message MsgHup() {
        Message message = new Message();
        message.setType(MsgType.MsgHup);
        message.setTo(null);
        return message;
    }

    static Message MsgVoteReq(int to, int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        MsgVoteReq message = new MsgVoteReq();
        message.setType(MsgType.MsgVote);
        message.setTo(to);
        message.setTerm(term);
        message.setCandidateId(candidateId);
        message.setLastLogIndex(lastLogIndex);
        message.setLastLogTerm(lastLogTerm);
        return message;
    }

    static Message MsgPreVoteReq(int to, int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        Message message = MsgVoteReq(to, term, candidateId, lastLogIndex, lastLogTerm);
        message.setType(MsgType.MsgPreVote);
        return message;
    }

    static Message MsgVoteResp(int to, int term, boolean voteGranted) {
        MsgVoteResp message = new MsgVoteResp();
        message.setType(MsgType.MsgVoteResp);
        message.setTo(to);
        message.setTerm(term);
        message.setVoteGranted(voteGranted);
        return message;
    }

    static Message MsgPreVoteResp(int to, int term, boolean voteGranted) {
        Message message = MsgVoteResp(to, term, voteGranted);
        message.setType(MsgType.MsgPreVoteResp);
        return message;
    }

}
