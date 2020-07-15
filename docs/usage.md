# 使用说明

## 服务器配置

可选使用/不使用 docker。

### tomcat+mysql+server.jar（不推荐）

以下安装以 ubuntu 为例

#### tomcat

用途：web 后端

安装 tomcat：

- 给 tomcat 建立一个工作目录（如 "/opt/tomcat"）
- 从 [tomcat 官网](http://tomcat.apache.org/)下载 tomcat8，解压到工作目录
- 创建 tomcat 用户（同时创建了 tomcat 组），运行命令 `sudo chown -R tomcat:tomcat 工作目录`

配置 tomcat（参考自原项目配置文档）：

使用以下命令查看 java 版本:

`sudo update-java-alternatives -l`

输出可能是这样的:

`java-1.8.0-openjdk-amd64 1081 /usr/lib/jvm/java-1.8.0-openjdkamd64`

你的 `JAVA_HOME`= 输出+"/jre" 比如上述输出对应的家目录是:

`JAVA_HOME= /usr/lib/jvm/java-1.8.0-openjdk-amd64/jre`

利用以上信息修改配置文件:

`$sudo vim /etc/systemd/system/tomcat.service `

复制以下信息到该文件中:(如果和你自己的 java 位置不同,记得修改其中的 `JAVA_HOME`)

```
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target
[Service]
Type=forking
Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -
Djava.security.egd=file:/dev/./urandom'
ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh
User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always
[Install]
WantedBy=multi-user.target
```

重新载入配置:

`$sudo systemctl daemon-reload`

启动 tomcat:

`$sudo systemctl start tomcat`

确认没有错误发生:

`$sudo systemctl status tomcat`

此时浏览器打开 localhost:8080 即可看到 tomcat 默认页面。

放通 8080 端口，具体操作视机器防火墙决定。此时远程访问服务器的 8080 端口，即可看到 tomcat ，默认界面。

设置开机启动 tomcat:

`sudo systemctl enable tomcat `

#### mysql

用途：数据库

安装 mysql：`sudo apt install mysql-server`

导入数据表：`mysql -u root -p <mysql.sql`

（密码暂时写死为 201314）

#### server.jar

用途：作为 coordinator 接受 client 的控制连接，维护数据库中的节点状态

先安装 JDK。安装配置 openjdk-8 的方法如下（参考自原项目配置文档）：

```
安装 openjdk-8-jdk：
$ sudo apt-get install openjdk-8-jdk

查看 java 版本：
$ java -version

编辑/etc/profile：
$ sudo vim /etc/profile

在文件尾添加 java 环境变量：
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/jre/bin" 
 ```

运行：

`java -jar server.jar`

### docker-compose （推荐）

以下使用以 Arch 为例

- 安装 docker 包或是 AUR 中的 docker-git 包及 docker-compose 包
- （推荐）添加自己的用户到 docker user group。
- （推荐）在 `/etc/docker/daemon.json` 加入一些 docker hub 镜像。
- （推荐）复制 ./src/docker 到一个新的位置，假设文件名为 panic。要确保服务所需要的网络端口已开启。
-  在主目录（也就是 docker-compose.yml 文件存在的目录）下输入 `docker-compose build`，即可开始构建服务。
- 使用 `docker-compose up` 即可启动服务，若不想看到服务的命令输出部分，可以加上 `-d` 参数即 `docker-compose up -d` 来分离日志输出。
- 若要停止服务，在终端里键入 `docker-compose down` 即可停止并删除容器，容器的数据（MySql 的数据表和 TOMCAT 的 webapps）均已进行数据持久化，不会因为删除容器而丢失所有数据。

## 存储节点配置(client)

安装 JDK，同上

若没有公网 IP，请连接 vpn 获取虚拟局域网 IP

配置文件命名为 setup.ini，格式如下（不包含注释）：

```
127.0.0.1                   //服务器IP
2333                        //服务器控制连接端口，即 server.jar 监听的端口
127.0.0.1                   //存储节点自身可以被寻址的IP，为公网IP/虚拟局域网IP
9998                        //存储节点监听的数据链接端口，浏览器将通过 websocket 连接该端口建立数据连接
1                           //存储节点的 ID（暂时由管理员分配）
/home/ubuntu/client_test    //存储节点用来存放文件碎片的目录，需要已经创建好
100                         //存储节点预设贡献的容量，单位为 MB

```

将 client.jar 与 setup.ini 置于同一目录下

```
+-- client.jar
+-- setup.ini
```

进入该目录，运行 `java -jar client.jar`

## 上传/下载(web)

如存储节点使用了 vpn，请连接 vpn （调试时可以单设备，地址写 127.0.0.1，浏览器和存储节点在同一设备）

调试说明：
- F12 查看 console，network  
- 测试用户名密码：xixi/123456
