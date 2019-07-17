package com.jraft.node;

import com.jraft.Config;
import com.jraft.Message.DisruptorEvent;
import com.jraft.Message.Message;
import com.jraft.raft.Raft;
import com.jraft.server.PeerClient;
import com.lmax.disruptor.EventHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责和网络层,状态机，持久化 交互
 */
@Getter
@Setter
public class Node implements EventHandler<DisruptorEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    private Config config;

    private Raft raft;

    private DisruptorQueue in; //消息入口

    private DisruptorQueue out;//消息出口

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

        PeerDispatcher peerDispatcher = new PeerDispatcher(n, config.getId(), config.getAddressMap());
        DisruptorQueue out = new DisruptorQueue(peerDispatcher);
        out.start();
        n.setOut(out);

        DisruptorQueue in = new DisruptorQueue(n);
        in.start();
        n.setIn(in);

        Raft raft = Raft.NewRaft(config, n);
        raft.becomeFollower(0, -1);
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
     * 发送消息
     */

    public void send(Message message) {
        if (message.getTo() == null) {//本地消息
            getIn().send(message);
        } else {
            getOut().send(message);
        }
    }

    /**
     * 处理消息
     *
     * @param message
     */
    public void step(Message message) {
        raft.step(message);
    }

    /**
     * 接收事件
     *
     * @param disruptorEvent
     * @param l
     * @param b
     * @throws Exception
     */
    @Override
    public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
        Message message = disruptorEvent.getMessage();
        LOGGER.info(String.format("处理消息序号%d,内容%s", l, message));
        step(message);
    }
}
