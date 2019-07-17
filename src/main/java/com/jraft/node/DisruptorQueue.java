package com.jraft.node;

import com.jraft.Message.DisruptorEvent;
import com.jraft.Message.DisruptorEventFactory;
import com.jraft.Message.Message;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author chenchang
 * @date 2019/7/16 15:10
 */
@Setter
@Getter
public class DisruptorQueue {

    private Disruptor<DisruptorEvent> disruptor;

    private RingBuffer<DisruptorEvent> ringBuffer;

    public DisruptorQueue(EventHandler<DisruptorEvent> handler) {
        DisruptorEventFactory eventFactory = new DisruptorEventFactory();
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        Disruptor<DisruptorEvent> disruptor = new Disruptor<DisruptorEvent>(eventFactory, 1048576, threadFactory, ProducerType.MULTI, new BusySpinWaitStrategy());
        this.disruptor = disruptor;
        disruptor.handleEventsWith(handler);
    }

    public void start() {
        this.ringBuffer = disruptor.start();
    }

    /**
     * 线程安全的 可多生成者
     *
     * @param message
     */
    public void send(Message message) {
        long next = ringBuffer.next();
        DisruptorEvent disruptorEvent = ringBuffer.get(next);
        disruptorEvent.setMessage(message);
        ringBuffer.publish(next);
    }

}
