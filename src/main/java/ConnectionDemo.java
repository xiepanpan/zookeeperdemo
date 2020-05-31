import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author: xiepanpan
 * @Date: 2020/5/30
 * @Description: zookeeper 连接
 */
public class ConnectionDemo {
    public static void main(String[] args) throws Exception {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper =
                    new ZooKeeper("192.168.217.130:2181,192.168.217.130:2183,192.168.217.130:2183", 4000, new Watcher() {
                        public void process(WatchedEvent watchedEvent) {
                            //如果收到了服务端的响应事件，连接成功
                            if (Event.KeeperState.SyncConnected==watchedEvent.getState()) {
                                countDownLatch.countDown();
                            }
                        }
                    });
            countDownLatch.await();
            //CONNECTED
            System.out.println(zooKeeper.getState());

            //创建节点 第三个参数是权限 OPEN_ACL_UNSAFE 所有人都可以访问 第四个参数 节点类型 持久节点
            zooKeeper.create("/zk-persistent-xpp","0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            Thread.sleep(1000);

            //得到节点
            Stat stat = new Stat();
            byte[] bytes = zooKeeper.getData("/zk-persistent-xpp", null, stat);
            System.out.println(new String(bytes));

            //修改节点
            zooKeeper.setData("/zk-persistent-xpp","1".getBytes(),stat.getVersion());

            byte[] data = zooKeeper.getData("/zk-persistent-xpp", null, stat);
            System.out.println(new String(data));

            //删除节点 使用乐观锁 比较版本号
            zooKeeper.delete("/zk-persistent-xpp",stat.getVersion());

            zooKeeper.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
