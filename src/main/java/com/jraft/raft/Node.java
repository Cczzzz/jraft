package com.jraft.raft;

import com.jraft.Config;
import com.jraft.Message.DisruptorEvent;
import com.jraft.Message.DisruptorQueue;
import com.jraft.Message.Message;
import com.lmax.disruptor.EventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * 负责和网络层,状态机，持久化 交互
 */
@Getter
@Setter
@Log4j
public class Node implements EventHandler<DisruptorEvent> {

    private Config config;

    private Raft raft;

    private DisruptorQueue in;

    private DisruptorQueue out;

    private Node() {
    }

    /**
     * 启动
     *
     * @param config
     * @return
     */
    public static Node startNode(Config config) {
        Node n = new Node();

        DisruptorQueue in = new DisruptorQueue(n);
        in.start();
        n.setIn(in);

        DisruptorQueue out = new DisruptorQueue(n);
        out.start();
        n.setOut(out);

        Raft raft = Raft.NewRaft(config, n);
        raft.becomeFollower(0, null);
        n.setRaft(raft);
        return n;
    }

    /**
     * 重启
     *
     * @param config
     * @return
     */
    public static Node restartNode(Config config) {
        Node n = new Node();
        return n;
    }

    /**
     * 处理消息
     *
     * @param message
     */
    public void step(Message message) {

    }

    /**
     * 处理事件的核心逻辑
     *
     * @param disruptorEvent
     * @param l
     * @param b
     * @throws Exception
     */
    @Override
    public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
        Message message = disruptorEvent.getMessage();
        log.info(String.format("处理消息序号%d,内容%s", l, message));
        step(message);
    }
}
