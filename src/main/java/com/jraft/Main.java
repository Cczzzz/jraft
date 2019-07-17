package com.jraft;


import lombok.extern.log4j.Log4j;

@Log4j
public class Main {


    public static void main(String[] args) throws InterruptedException {
        String[] peerAddress = new String[]{"127.0.0.1:8888", "127.0.0.1:9999"};

        Config config = Config.newBuilder()
                .localPort(8888)
                .peerSocketAddresses(peerAddress)
                .preVote(true)
                .electionInterval(1000)
                .heartbeatInterval(100)
                .build();

        JraftServer server = JraftServer.newJraftServer(config);
        server.start();
        log.info("启动成功");


        Config config2 = Config.newBuilder()
                .localPort(9999)
                .peerSocketAddresses(peerAddress)
                .preVote(true)
                .electionInterval(1000)
                .heartbeatInterval(100)
                .build();

        JraftServer server2 = JraftServer.newJraftServer(config2);
        server2.start();
        log.info("启动成功");

    }
}
