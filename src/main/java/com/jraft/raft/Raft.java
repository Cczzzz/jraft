package com.jraft.raft;


import com.jraft.Config;
import com.jraft.Message.Message;
import com.jraft.Message.MsgHearBeat;
import com.jraft.Message.MsgType;
import com.jraft.node.Node;
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
    private int term; //当前任期
    private Integer vote; //当前投票
    private volatile int lead; //当前leader
    private volatile int State; //状态

    private boolean checkQuorum; //选举之前校验是否能于集群中大多数节点通信
    private boolean preVote; //是否开启pre 在选举之前进行一次pre 选举

    private int electionInterval; //选举计时 间隔
    private int heartbeatInterval;//心跳计时 间隔

    private AtomicInteger electionElapsed = new AtomicInteger(0);//选举计时已运行的时间
    private AtomicInteger heartbeatElapsed = new AtomicInteger(0);//心跳计时已运行的时间
    private int tickInterval = 10;//计时任务间隔

    private RaftLog raftLog; //日志存储
    private PeerTracker peerTracker = new PeerTracker();// 维护其它节点
    private Node node;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); // 定时任务
    private volatile Consumer<Message> step;

    private Config config;

    private Raft(Config config, Node node) {
        this.config = config;
        id = config.getId();
        this.node = node;

        electionInterval = config.getElectionInterval();
        heartbeatInterval = config.getHeartbeatInterval();

    }

    public void reset(int term) {
        if (this.term != term) {
            this.term = term;
            this.vote = null;
        }
        this.lead = -1;
        peerTracker.resetVotes();
        electionElapsed.set(0);
        heartbeatElapsed.set(0);
    }

    public void becomeFollower(int term, Integer lead) {
        reset(term);
        this.lead = lead;
        step = stepFollower;

    }

    public static Raft NewRaft(Config config, Node node) {
        Raft raft = new Raft(config, node);
        //todo 重启时加载日志
        RaftLog raftLog = new RaftLog();
        raft.setRaftLog(raftLog);
        config.getAddressMap().keySet().forEach(k -> {
            raft.peerTracker.initPeer(k, 0, 1);
        });
        raft.runTick();
        return raft;
    }

    /**
     * 用于执行定时任务 不断推进计时器
     */
    public void runTick() {
        Runnable tick = () -> {
            int election = electionElapsed.getAndAdd(10);
            int heartbeat = heartbeatElapsed.getAndAdd(10);
            if (id != lead && State < StateCandidate) {
                if (election >= electionInterval) {
                    //todo 发送选举
                    //选举计时器超时
                    electionElapsed.set(0);
                }
            } else {
                if (heartbeat >= heartbeatInterval) {
                    send(MsgHearBeat.MsgBeat());
                    heartbeatElapsed.set(0);
                }
            }
        };
        service.scheduleAtFixedRate(tick, 0, 10, TimeUnit.MILLISECONDS);
    }


    /**
     * 传发给node
     *
     * @param message
     */
    public void send(Message message) {
        node.send(message);
    }

    /**
     * 处理消息
     *
     * @param message
     */
    public void step(Message message) {
        if (message.getType().equals(MsgType.MsgBeat)) {
            for (Peer peer : peerTracker.getPeers()) {
                send(MsgHearBeat.MsgHeartbeat(peer.getId(), term, lead, 0, null, 0, 0, raftLog.getLastCommittde()));
            }
        }
    }

    /**
     * 处理同步日志相关
     * <p>
     * 如果 term < currentTerm 就返回 false （5.1 节）
     * 如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false （5.3 节）
     * 如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的 （5.3 节）
     * 附加日志中尚未存在的任何新条目
     * 如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
     */
    public void stepLog() {

    }

    Consumer<Message> stepFollower = message -> {
    };
    Consumer<Message> stepCandidate = message -> {
    };
    Consumer<Message> stepLeader = message -> {
    };


}
