import com.jraft.Config;
import com.jraft.JraftServer;
import org.junit.Test;

import java.net.InetAddress;

/**
 * @author chenchang
 * @date 2019/7/19 3:37
 */
public class Server {
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        InetAddress ia = null;
        try {
            ia = ia.getLocalHost();

            String localname = ia.getHostName();
            String localip = ia.getHostAddress();
            System.out.println("本机名称是：" + localname);
            System.out.println("本机的ip是 ：" + localip);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void sever1() throws InterruptedException {
        String[] peerAddress = new String[]{"127.0.0.1:7777", "127.0.0.1:9999"};

        Config config = Config.newBuilder()
                .localSocketAddresses("127.0.0.1", 8888)
                .peerSocketAddresses(peerAddress)
                .preVote(true)
                .electionInterval(1000)
                .heartbeatInterval(100)
                .build();

        JraftServer server = JraftServer.newJraftServer(config);
        server.start();
        while (true){
            Thread.sleep(500000000);
        }
    }

    @Test
    public void sever2() throws InterruptedException {

        String[] peerAddress2 = new String[]{"127.0.0.1:7777", "127.0.0.1:8888"};
        Config config2 = Config.newBuilder()
                .localSocketAddresses("127.0.0.1", 9999)
                .peerSocketAddresses(peerAddress2)
                .preVote(true)
                .electionInterval(2000)
                .heartbeatInterval(100)
                .build();

        JraftServer server2 = JraftServer.newJraftServer(config2);
        server2.start();
        while (true){
            Thread.sleep(500000000);
        }
    }

    @Test
    public void sever3() throws InterruptedException {
        String[] peerAddress3 = new String[]{"127.0.0.1:8888", "127.0.0.1:9999"};

        Config config3 = Config.newBuilder()
                .localSocketAddresses("127.0.0.1", 7777)
                .peerSocketAddresses(peerAddress3)
                .preVote(true)
                .electionInterval(2000)
                .heartbeatInterval(100)
                .build();

        JraftServer server3 = JraftServer.newJraftServer(config3);
        server3.start();
        while (true){
            Thread.sleep(500000000);
        }
    }
}
