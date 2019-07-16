package com.jraft;

import com.jraft.raft.Node;
import com.jraft.server.HttpServer;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.IOException;

/**
 * @author chenchang
 * @date 2019/7/15 20:24
 */
@Setter
@Getter
@Log4j
public class JraftServer implements java.io.Closeable {

    private Config config;

    private volatile boolean started = false;

    private JraftServer(Config config) {
        this.config = config;
    }

    public static JraftServer newJraftServer(Config config) {
        JraftServer server = new JraftServer(config);
        return server;
    }

    public void start() throws InterruptedException {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            Node node = Node.startNode(config);
            HttpServer server = new HttpServer(config, node);
            ChannelFuture startServer = server.start();
            if (startServer.isSuccess()) {
                log.info("jraft httpServer start !");
            } else {
                log.info("jraft httpServer fall  !");
                //todo 抛异常
            }
        }
    }


    @Override
    public void close() throws IOException {

    }
}
