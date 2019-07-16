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
}
