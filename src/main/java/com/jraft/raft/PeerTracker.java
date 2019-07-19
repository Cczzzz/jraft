package com.jraft.raft;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenchang 管理其它节点状态 和投票
 * @date 2019/7/16 18:24
 */
@Setter
@Getter
public class PeerTracker {

    private Map<Integer, Peer> map = new HashMap<>();

    private List<Peer> peers = new ArrayList<>();

    private Map<Integer, Boolean> votes = new HashMap<>();

    public final static int electionWon = 0;
    public final static int electionLost = 1;
    public final static int electionIndeterminate = 2;

    public void initPeer(int id, int matchIndex, int nextIndex) {
        Peer peer = new Peer(id, matchIndex, nextIndex);
        peers.add(peer);
        map.put(id, peer);
    }

    public void resetVotes() {
        votes = new HashMap<>();
    }

    /**
     * 统计投票
     *
     * @param id
     */
    public int poll(int id, boolean voteGranted) {
        votes.put(id, voteGranted);
        int half = quorum();
        int grantedTure = 0;
        int grantedFalse = 0;
        for (Boolean value : votes.values()) {
            if (value) {
                grantedTure++;
            } else {
                grantedFalse++;
            }
        }
        if (grantedTure >= half) {
            return electionWon;
        }
        if (grantedFalse >= half) {
            return electionLost;
        }

        return electionIndeterminate;
    }

    /**
     * 查询id 是否在当前集群
     *
     * @param id
     * @return
     */
    public boolean isPeer(int id) {
        return map.containsKey(id);
    }

    /**
     * 查询是否能与大多数节点通信
     *
     * @return
     */
    public boolean quorumActive() {
        return true;
    }

    /**
     * @return
     */
    private int quorum() {
        return peers.size() / 2;
    }

}
