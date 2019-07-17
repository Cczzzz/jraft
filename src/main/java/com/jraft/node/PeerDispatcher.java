package com.jraft.node;

import com.jraft.Message.DisruptorEvent;
import com.jraft.Message.Message;
import com.jraft.server.PeerClient;
import com.lmax.disruptor.EventHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author chenchang 负责在队列中取出消息 通过client 发出
 * @date 2019/7/17 22:03
 */
@Getter
@Setter
public class PeerDispatcher implements EventHandler<DisruptorEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerDispatcher.class);
    private PeerClient client;
    private Map<Integer, InetSocketAddress> addressMap;
    private int id;

    public PeerDispatcher(Node node, int id, Map<Integer, InetSocketAddress> addressMap) {
        this.client = new PeerClient(node, addressMap);
        this.addressMap = addressMap;
        this.id = id;

    }

    @Override
    public void onEvent(DisruptorEvent disruptorEvent, long l, boolean b) throws Exception {
        Message message = disruptorEvent.getMessage();
        message.setFrom(id);
        Integer toId = message.getTo();
        if (toId != null) {
            try {
                client.send(toId, message);
            } catch (Throwable throwable) {
                LOGGER.info("发送失败");
                throwable.printStackTrace();
            }
        }
    }
}
