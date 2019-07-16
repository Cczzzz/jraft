package com.jraft.raft;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenchang
 * @date 2019/7/16 18:24
 */
@Setter
@Getter
public class PeerTracker {

    private Map<Integer, Peer> map = new HashMap<>();

    private List<Peer> peers = new ArrayList<>();

    private Map<Integer, Boolean> votes = new HashMap<>();

    public void initPeer(int id, int matchIndex, int nextIndex) {
        Peer peer = new Peer(id, matchIndex, nextIndex);
        peers.add(peer);
        map.put(id, peer);
    }

    public void resetVotes() {
        votes = new HashMap<>();
    }

}
