package com.jraft.raft;

/**
 * @author chenchang
 * @date 2019/7/15 21:31
 */
public class RaftLog {

    /**
     * 已提交的日志索引
     */
    private int committde;
    /**
     * 已应用的日志索引
     */
    private int applied;

    /**
     * 最新的日志索引
     *
     * @return
     */
    public int getLastIndex() {
        return 0;
    }

    /**
     * 最新一条提交的index
     *
     * @return
     */
    public int getLastCommittde() {
        return committde;
    }

    /**
     * 最新一条应用的index
     *
     * @return
     */
    public int getLastApplied() {
        return applied;
    }

    /**
     * index 的上一条日志的index
     *
     * @return
     */
    public int getPrevLogIndex(int index) {
        return applied;
    }

    /**
     * index 的上一条日志的term
     *
     * @param index
     * @return
     */
    public int getPrevLogTerm(int index) {
        return applied;
    }

}
