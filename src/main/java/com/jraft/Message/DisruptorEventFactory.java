package com.jraft.Message;

/**
 * @author chenchang
 * @date 2019/7/16 15:14
 */
public class DisruptorEventFactory implements com.lmax.disruptor.EventFactory<DisruptorEvent> {

    @Override
    public DisruptorEvent newInstance() {
        return new DisruptorEvent();
    }
}
