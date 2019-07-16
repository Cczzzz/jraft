package com.jraft.raft;


import com.jraft.Config;
import com.jraft.Message.Message;
import com.sun.xml.internal.bind.v2.model.core.ID;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author chenchang
 * @date 2019/6/25 17:59
 * 负责raft算法实现
 */
@Getter
@Setter
public class Raft {
    private final int StateFollower = 1;
    private final int StateCandidate = 2;
    private final int StatePreCandidate = 4;
    private final int StateLeader = 3;

    private int id; //本节点id
    private Integer term; //当前任期
    private Integer vote; //当前投票
    private volatile Integer lead; //当前leader
    private volatile int State; //状态

    private boolean checkQuorum; //选举之前校验是否能于集群中大多数节点通信
    private boolean preVote; //是否开启pre 在选举之前进行一次pre 选举

    private int electionInterval; //选举计时 间隔
    private int heartbeatInterval;//心跳计时 间隔

    private AtomicInteger electionElapsed;//选举计时已运行的时间

    private RaftLog raftLog; //日志存储
    private PeerTracker peerTracker = new PeerTracker();// 维护其它节点
    private Node node;
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); // 定时任务

    private Raft(Config config, Node node) {
        id = config.getId();
        this.node = node;
    }

    public void reset(int term) {
        if (this.term != term) {
            this.term = term;
            this.vote = null;
        }
        this.lead = null;
        peerTracker.resetVotes();

    }

    public void becomeFollower(int term, Integer lead) {
        reset(term);
        this.lead = lead;
        electionElapsed.set(0);
    }

    public static Raft NewRaft(Config config, Node node) {
        Raft raft = new Raft(config, node);
        config.getAddressMap().keySet().forEach(k -> {
            raft.peerTracker.initPeer(k, 0, 1);
        });
        raft.runTick();
        return raft;
    }

    /**
     * 用于执行定时任务
     */
    public void runTick() {
        service.scheduleWithFixedDelay(tick, 100, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    Runnable tick = () -> {
        int election = electionElapsed.getAndAdd(heartbeatInterval);
        if (id != lead && State < StateCandidate) {
            if (election >= electionInterval) {
                //todo 发送选举
                //选举计时器超时
                electionElapsed.set(0);
            }
        } else {
            send(Message.MsgBeat());
        }
    };

    /**
     * 传发给node node会再发给raft 转化为单线程处理
     *
     * @param message
     */
    public void send(Message message) {
        node.getIn().send(message);
    }

    Consumer<Message> stepLeader = message -> {
    };
    Consumer<Message> stepFollower = message -> {
    };
    Consumer<Message> stepCandidate = message -> {
    };


}
