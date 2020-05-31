import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author: xiepanpan
 * @Date: 2020/5/30
 * @Description:
 */
public class WatcherDemo {
    public static void main(String[] args) {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final ZooKeeper zooKeeper =
                    new ZooKeeper("192.168.217.130:2181,192.168.217.130:2183,192.168.217.130:2183", 4000, new Watcher() {
                        public void process(WatchedEvent watchedEvent) {
                            System.out.println("默认事件："+watchedEvent.getType());
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

            //通过exists绑定监听
            Stat stat = zooKeeper.exists("/zk-persistent-xpp", new Watcher() {
                public void process(WatchedEvent event) {
                    //事件类型 节点路径
                    System.out.println("节点事件"+event.getType() + "，节点路径" + event.getPath());
                    try {
                        //得到通知后再一次绑定监听 从而接收到下一次的通知
                        zooKeeper.exists(event.getPath(),true);
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            //通过修改的事务类型操作来触发监听事件
            stat = zooKeeper.setData("/zk-persistent-xpp", "2".getBytes(), stat.getVersion());

            Thread.sleep(1000);

            //删除也会触发监听事件
            zooKeeper.delete("/zk-persistent-xpp",stat.getVersion());
            zooKeeper.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

}
