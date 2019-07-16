package com.jraft;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        String[] peerAddress = new String[]{"127.0.0.1:8888", "127.0.0.1:9999"};

        Config config = Config.newBuilder()
                .localPort(7777)
                .peerSocketAddresses(peerAddress)
                .preVote(true)
                .build();

        JraftServer server = JraftServer.newJraftServer(config);
        server.start();


    }
}
