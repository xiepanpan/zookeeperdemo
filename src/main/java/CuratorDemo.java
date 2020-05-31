import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author: xiepanpan
 * @Date: 2020/5/31
 * @Description:  使用Curator框架来对zookeeper操作
 */
public class CuratorDemo {
    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                //衰减的重试机制
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .connectString("192.168.217.130:2181,192.168.217.130:2183,192.168.217.130:2183")
                .sessionTimeoutMs(4000)
                //隔离命名空间  以下所有操作都是基于该相对目录进行的
                .namespace("curator")
                .build();
        curatorFramework.start();

        //创建节点
        //结果 /curator/xpp/mode1
        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/xpp/node1","1".getBytes());

        //更改节点
        //保存节点状态
        Stat stat = new Stat();
        curatorFramework.getData().storingStatIn(stat).forPath("/xpp/node1");
        curatorFramework.setData().withVersion(stat.getVersion()).forPath("/xpp/node1","xx".getBytes());

        //删除节点
        curatorFramework.delete().deletingChildrenIfNeeded().forPath("/xpp/node1");
    }



}
