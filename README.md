# jraft
a java raft realize,reference etcd

分布式一致性raft算法的java实现
基于raft论文实现，参照etcd源码
使用netty作为server暂时使用http协议
支持集群选举和pre模式，日志复制暂不支持持久化
