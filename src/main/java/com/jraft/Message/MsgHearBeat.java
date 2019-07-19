package com.jraft.Message;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenchang
 * @date 2019/7/17 22:15
 */
public interface MsgHearBeat {

    @Getter
    @Setter
    class MsgHeartbeatResp extends Message {
        // private Integer term; //当前的任期号，用于领导人去更新自己
        private Boolean success; //跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真

        @Override
        public String toString() {
            return "MsgHeartbeatResp{" +
                    "success=" + success +
                    "} " + super.toString();
        }
    }

    static Message MsgBeat() {
        Message message = new Message();
        message.setType(MsgType.MsgBeat);
        message.setTo(null);
        return message;
    }

    /**
     * @param to           接受方id
     * @param term         当前任期
     * @param leaderld     leader id
     * @param index        最新日志
     * @param entries      日志数据
     * @param commit       提交的日志
     * @param prevLogIndex 新日志的上一条日志
     * @param prevLogTerm  新日志的上一条的任期
     * @return
     */
    static Message MsgHeartbeat(int to, int term, int leaderld, int index, Entry[] entries, int commit, int prevLogIndex, int prevLogTerm) {
        Message message = new Message();
        message.setType(MsgType.MsgHeartbeat);
        message.setTo(to);
        message.setTerm(term);
        message.setLeaderId(leaderld);
        message.setLastLogIndex(index);
        message.setCommit(commit);
        message.setEntries(entries);
        message.setPrevLogIndex(prevLogIndex);
        message.setPrevLogTerm(prevLogTerm);
        return message;
    }

    static Message MsgHeartbeatResp(int to, int term, Boolean success) {
        MsgHeartbeatResp message = new MsgHeartbeatResp();
        message.setTo(to);
        message.setTerm(term);
        message.setSuccess(success);
        return message;
    }


}
