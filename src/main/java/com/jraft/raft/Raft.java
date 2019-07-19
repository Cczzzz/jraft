package com.jraft.raft;


import com.jraft.Config;
import com.jraft.Message.*;
import com.jraft.kit.AvertNull;
import com.jraft.node.Node;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Raft.class);

    private final static int stateFollower = 1;
    private final static int statePreCandidate = 2;
    private final static int stateCandidate = 3;
    private final static int stateLeader = 4;

    private int id; //本节点id
    private int term; //当前任期
    private Integer vote; //当前投票
    private int lead; //当前leader
    private int state; //状态

    private boolean checkQuorum; //选举之前校验是否能于集群中大多数节点通信
    private boolean preVote; //是否开启pre 在选举之前进行一次pre 选举

    private int electionInterval; //选举计时 间隔
    private int heartbeatInterval;//心跳计时 间隔
    private int tickInterval = 10;//计时任务间隔

    private AtomicInteger electionElapsed = new AtomicInteger(0);//选举计时已运行的时间
    private AtomicInteger heartbeatElapsed = new AtomicInteger(0);//心跳计时已运行的时间

    private RaftLog raftLog; //日志存储
    private PeerTracker peerTracker = new PeerTracker();// 维护其它节点状态
    private Node node;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(); // 定时任务

    /**
     * 不同状态处理消息的函数
     *
     * @param <T>
     */
    private interface stepFunction<T> {
        void step(T t);
    }

    private stepFunction<Message> step;

    private Config config;

    private Raft(Config config, Node node) {
        this.config = config;
        this.preVote = config.isPreVote();
        this.id = config.getId();
        this.node = node;

        electionInterval = config.getElectionInterval();
        heartbeatInterval = config.getHeartbeatInterval();
    }

    private void reset(int term) {
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
        state = stateFollower;
        LOGGER.info("更变状态 to Follower");
    }

    public void becomePreCandidate(int term) {
        reset(term);
        step = stepCandidate;
        state = statePreCandidate;
        LOGGER.info("更变状态 to PreCandidate");
    }

    public void becomeCandidate(int term) {
        reset(term + 1);
        vote = id;
        step = stepCandidate;
        state = stateCandidate;
        LOGGER.info("更变状态 to Candidate");

    }

    public void becomeLeader(int term) {
        reset(term);
        this.lead = id;
        step = stepLeader;
        state = stateLeader;
        LOGGER.info("更变状态 to Leader");

    }


    public static Raft NewRaft(Config config, Node node) {
        Raft raft = new Raft(config, node);
        //todo 重启时加载日志
        RaftLog raftLog = new RaftLog();
        raft.setRaftLog(raftLog);
        config.getAddressMap().keySet().forEach(k -> raft.peerTracker.initPeer(k, 0, 1));
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
            if (state <= stateCandidate && election >= electionInterval) {
                send(MsgVote.MsgHup());//心跳超时事件
                electionElapsed.set(0);
            }
            if (lead == id && heartbeat >= heartbeatInterval) {
                send(MsgHearBeat.MsgBeat());//选举超时事件
                heartbeatElapsed.set(0);
            }
        };
        service.scheduleAtFixedRate(tick, (long) (Math.random() * heartbeatInterval), tickInterval, TimeUnit.MILLISECONDS);
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
        // 本地消息
        switch (message.getType()) {
            case MsgType.MsgHup:// 选举超时事件
                initiateVote();
                break;
        }
        //请求是集群中的节点
        if (message.getFrom() != null && peerTracker.isPeer(message.getFrom())) {
            //投票请求
            if ((message.getType() == MsgType.MsgVote || message.getType() == MsgType.MsgPreVote)) {
                stepVote((MsgVote.MsgVoteReq) message);
                return;
            }
            if (message.getType() == MsgType.MsgHeartbeat && message.getTerm() < term) {
                send(MsgHearBeat.MsgHeartbeatResp(message.getFrom(), term, false));
                return;
            }
        }
        step.step(message);
    }


    /**
     * 处理投票逻辑
     * 如果term < currentTerm返回 false （5.2 节）
     * 如果 votedFor 为空或者为 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     *
     * @return
     */
    private void stepVote(MsgVote.MsgVoteReq message) {
        boolean voteGranted =
                //已经投过票 可以直接给他投票
                (vote != null && vote.equals(message.getCandidateId())) ||
                        //没投过票并且 & 没有lead & 任期大于当前
                        ((vote == null && lead == -1) && term < message.getTerm());
        //查询日志是否比本地新
        boolean isUpdate = raftLog.isUpToDate(message.getLastLogIndex(), message.getLastLogTerm());
        voteGranted = voteGranted & isUpdate;
        Message resp = null;
        if (message.getType().equals(MsgType.MsgVote)) {
            //pre投票不记录
            if (voteGranted) {
                vote = message.getCandidateId();
                electionElapsed.set(0);
            }
            resp = MsgVote.MsgVoteResp(message.getFrom(), term, voteGranted);
        } else {
            resp = MsgVote.MsgPreVoteResp(message.getFrom(), term, voteGranted);
        }

        send(resp);
    }

    /**
     * 发起投票
     */
    private void initiateVote() {
        if (state <= stateCandidate) {
            if (state == stateFollower) {
                if (preVote) {
                    becomePreCandidate(term);
                } else {
                    becomeCandidate(term);
                }
            } else if (state == statePreCandidate) {
                becomePreCandidate(term);
            } else {
                becomeCandidate(term);
            }
            Entry lastLog = raftLog.getLastLogEntry();
            for (Peer peer : peerTracker.getPeers()) {
                if (state == statePreCandidate) {
                    send(MsgVote.MsgPreVoteReq(peer.getId(), term + 1, id, lastLog.getIndex(), lastLog.getTerm()));
                } else {
                    send(MsgVote.MsgVoteReq(peer.getId(), term, id, lastLog.getIndex(), lastLog.getTerm()));
                }
            }
        }
    }


    stepFunction<Message> stepFollower = message -> {
        switch (message.getType()) {
            case MsgType.MsgApp:
                lead = message.getFrom();
                electionElapsed.set(0);
                term = message.getTerm();
                break;
            case MsgType.MsgHeartbeat:
                lead = message.getFrom();
                electionElapsed.set(0);
                term = message.getTerm();
                LOGGER.info("Heartbeat !");
                break;
        }

    };
    stepFunction<Message> stepCandidate = message -> {
        switch (message.getType()) {
            case MsgType.MsgApp:
                becomeFollower(message.getTerm(), message.getFrom());
                break;
            case MsgType.MsgHeartbeat:
                becomeFollower(message.getTerm(), message.getFrom());
                break;
        }
        if ((message.getType() == MsgType.MsgPreVoteResp || message.getType() == MsgType.MsgVoteResp)) {
            //投票响应
            MsgVote.MsgVoteResp msgVoteResp = (MsgVote.MsgVoteResp) message;
            //计入投票桶
            int election = peerTracker.poll(msgVoteResp.getFrom(), msgVoteResp.getVoteGranted());
            if (election == PeerTracker.electionWon) {
                if (state == statePreCandidate) {
                    LOGGER.info("election won !");
                    becomeCandidate(term + 1);
                    initiateVote();
                } else {
                    becomeLeader(term);
                }
            } else if (election == PeerTracker.electionLost) {
                LOGGER.info("election lost !");
                becomeFollower(term, -1);
            }
        }
    };
    stepFunction<Message> stepLeader = message -> {
        switch (message.getType()) {
            case MsgType.MsgBeat: //心跳超时事件
                if (lead == id) {
                    for (Peer peer : peerTracker.getPeers()) {
                        send(MsgHearBeat.MsgHeartbeat(peer.getId(), term, lead, 0, null, 0, 0, raftLog.getLastCommittde()));
                    }
                }
                break;

        }
    };


}
