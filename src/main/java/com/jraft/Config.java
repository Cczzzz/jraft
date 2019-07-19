package com.jraft;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 全局配置
 */
@Getter
@Setter
public class Config {
    /**
     * 本机id
     */
    private int id;
    /**
     * 本节点地址
     */
    private InetSocketAddress localSocketAddress;
    /**
     * 其它节点地址
     */
    private List<InetSocketAddress> PeerSocketAddresses;
    /**
     * pre 模式
     */
    private boolean preVote;
    /**
     * 选举计时
     */
    private int electionInterval = 1000;
    /**
     * 心跳计时
     */
    private int heartbeatInterval = 100;

    private Map<Integer, InetSocketAddress> addressMap = new HashMap<>();

    private Config() {
    }

    public void setPeerSocketAddresses(String... peer) {
        //todo 校验
        List<InetSocketAddress> peers =
                Arrays.stream(peer).
                        map(s -> s.split(":")).
                        filter(strings -> strings.length == 2).
                        map(strings -> new InetSocketAddress(strings[0], Integer.valueOf(strings[1]))).collect(Collectors.toList());
        PeerSocketAddresses = peers;
    }

    public void setPeerSocketAddresses(List<InetSocketAddress> peerSocketAddresses) {
        PeerSocketAddresses = peerSocketAddresses;
    }

    /**
     * 初始化
     */
    private void init() {
        for (InetSocketAddress socketAddress : PeerSocketAddresses) {
            int hashId = socketAddress.toString().hashCode();
            addressMap.put(hashId, socketAddress);
        }
        id = localSocketAddress.toString().hashCode();
        //todo 校验计时器

    }

    public static ConfigBuilder newBuilder() {
        Config config = new Config();
        return new ConfigBuilder(config);
    }

    /**
     * 构建者
     */
    public static class ConfigBuilder {
        private Config config;

        public ConfigBuilder(Config config) {
            this.config = config;
        }


        public ConfigBuilder localSocketAddresses(String host, int port) {
            this.config.setLocalSocketAddress(new InetSocketAddress(host, port));
            return this;
        }

        public ConfigBuilder peerSocketAddresses(String... peer) {
            this.config.setPeerSocketAddresses(peer);
            return this;
        }

        public ConfigBuilder peerSocketAddresses(List<InetSocketAddress> PeerSocketAddresses) {
            this.config.setPeerSocketAddresses(PeerSocketAddresses);
            return this;
        }

        public ConfigBuilder preVote(boolean preVote) {
            this.config.setPreVote(preVote);
            return this;
        }

        public ConfigBuilder electionInterval(int electionInterval) {
            this.config.setElectionInterval(electionInterval);
            return this;
        }

        public ConfigBuilder heartbeatInterval(int heartbeatInterval) {
            this.config.setHeartbeatInterval(heartbeatInterval);
            return this;
        }

        public Config build() {
            this.config.init();
            return config;

        }


    }


}
