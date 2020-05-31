import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author: xiepanpan
 * @Date: 2020/5/31
 * @Description:  事件监听
 */
public class CuratorWatcherDemo {

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

//        addListenerWithNodeCache(curatorFramework,"/xpp");
//        addListenerWithPathChildCache(curatorFramework,"/xpp");
        addListenerWithTreeCache(curatorFramework,"/xpp");
        System.in.read();
    }

    /**
     * 监听一个节点的更新和创建事件
     * @param curatorFramework
     * @param path
     * @throws Exception
     */
    public static void addListenerWithNodeCache(CuratorFramework curatorFramework,String path) throws Exception {
        //第三个参数 对详细内容数据的压缩
        final NodeCache nodeCache = new NodeCache(curatorFramework,path,false);
        NodeCacheListener nodeCacheListener = new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                System.out.println("Receive Event:"+nodeCache.getCurrentData().getPath());
            }
        };
        nodeCache.getListenable().addListener(nodeCacheListener);
        nodeCache.start();
    }

    /**
     *  监听一个节点子节点的创建删除和更新
     * @param curatorFramework
     * @param path
     * @throws Exception
     */
    public static void addListenerWithPathChildCache(CuratorFramework curatorFramework,String path) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path, true);
        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                System.out.println("Receive Event:"+pathChildrenCacheEvent.getType());
            }
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start(PathChildrenCache.StartMode.NORMAL);
    }

    /**
     * 综合节点监听事件 监听当前节点和子节点  节点上任何一个事件都能收到
     * @param curatorFramework
     * @param path
     * @throws Exception
     */
    public static void addListenerWithTreeCache(CuratorFramework curatorFramework,String path) throws Exception {
        TreeCache treeCache = new TreeCache(curatorFramework,path);
        TreeCacheListener treeCacheListener = new TreeCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                System.out.println(treeCacheEvent.getType()+"->"+treeCacheEvent.getData().getPath());
            }
        };
        treeCache.getListenable().addListener(treeCacheListener);
        treeCache.start();
    }
}
