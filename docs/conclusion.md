# 结题报告

- [结题报告](#结题报告)
	- [项目介绍](#项目介绍)
	- [立项依据](#立项依据)
		- [项目背景](#项目背景)
		- [相关项目](#相关项目)
			- [IPFS](#ipfs)
			- [NAS](#nas)
			- [前人工作](#前人工作)
			- [小结](#小结)
	- [项目设计](#项目设计)
		- [项目结构](#项目结构)
		- [Docker 容器化服务端](#docker-容器化服务端)
			- [为什么要使用容器化技术](#为什么要使用容器化技术)
			- [Docker-Compose](#docker-compose)
		- [前端美化](#前端美化)
		- [文件编解码](#文件编解码)
			- [Reed-Solomon 编码](#reed-solomon-编码)
			- [编解码原理](#编解码原理)
				- [编码](#编码)
				- [解码](#解码)
			- [稳定性](#稳定性)
			- [编码矩阵](#编码矩阵)
				- [基于范德蒙德（Vandermonde）矩阵](#基于范德蒙德vandermonde矩阵)
				- [基于柯西（Cauchy）矩阵](#基于柯西cauchy矩阵)
				- [柯西编解码过程优化](#柯西编解码过程优化)
			- [开源纠删码项目](#开源纠删码项目)
			- [WebAssembly](#webassembly)
			- [WebAssembly 与 JavaScript 效率对比](#webassembly-与-javascript-效率对比)
			- [浏览器端实现文件编解码](#浏览器端实现文件编解码)
				- [使用 FileReader 获取本地文件](#使用-filereader-获取本地文件)
				- [Go-WebAssembly：使用 syscall/js 包编写源代码](#go-webassembly使用-syscalljs-包编写源代码)
				- [callEncoder：接收原始数据并调用 Go 函数编码](#callencoder接收原始数据并调用-go-函数编码)
				- [callMD5：为碎片生成 MD5 摘要](#callmd5为碎片生成-md5-摘要)
				- [callDecoder：接收碎片并调用 Go 函数解码](#calldecoder接收碎片并调用-go-函数解码)
			- [Go-WebAssembly 编码性能](#go-webassembly-编码性能)
		- [WebSocket](#websocket)
		- [碎片分配策略](#碎片分配策略)
		- [多用户权限支持](#多用户权限支持)
			- [前置项目关于用户权限的设计](#前置项目关于用户权限的设计)
				- [数据库配置](#数据库配置)
			- [改进用到的技术](#改进用到的技术)
				- [新的数据库设计](#新的数据库设计)
				- [新的 Web 端设计](#新的-web-端设计)
	- [未来工作展望](#未来工作展望)
		- [重新加入中心节点](#重新加入中心节点)
		- [更高性能的纠删码模块设计](#更高性能的纠删码模块设计)
	- [项目总结](#项目总结)
	- [组员总结](#组员总结)
	- [致谢](#致谢)
	- [参考文献](#参考文献)

## 项目介绍

本项目旨在实现可用性高的基于互联网网页的小型分布式文件系统。为了减少不必要的重复劳动，本项目基于[往届 OSH 项目](https://github.com/IngramWang/DFS_OSH2017_USTC)进行优化。在已有的项目的基础上，实现容器化服务器端、多用户权限支持、更高效的文件传输、减轻中央服务器负担、提高文件安全性和可用性等优化。一方面做出易于部署可用性高的“私人网盘”，另一方面也为用户群的扩展和服务推广留出了上升空间。

## 立项依据

### 项目背景

随着社会经济的发展与信息化进程的继续，台式计算机、膝上电脑、智能手机、平板电脑和更多的智能可穿戴设备正疯狂涌入当前的家庭和小微企业。这些设备极大地提高了企业的办公效率、丰富了家庭的娱乐需求，但如何高效地利用分散在这些不同的设备上的存储空间如今正越发成为大家关注的问题：运用好这些分散的存储空间不仅可以方便多人合作，更可以避免资源的浪费。

而家庭和小微企业的存储设备有如下特点：

- 存储资源小而分散，每个设备的存储容量通常不超过 1 TB；
- 设备通常只有在使用时才会在线联网，否则处于关闭状态；
- 很多设备的位置随时间而变化，故它们常常并不总在其归属网络上；
- 和专用的服务器相比，这些设备的性能较低；
- 设备没有统一的指令集和操作系统；
- 连接设备的网络环境较差，往往通过一般的局域网或互联网相连接。

面对这些特点，很难用一个集中式的文件系统组织分散在这些不同的设备上的存储空间。

然而，即使是传统的分布式文件系统想在这种环境中应用也是十分困难的，这体现在：

- 传统的分布式文件系统往往要求高性能、稳定的服务器，而上述环境中的机器不但性能不足，更不常在线；
- 传统的分布式文件系统往往要求服务器具有相同的操作系统甚至是定制的操作系统以方便管理，而上述环境中的机器运行不同的操作系统上；

同时，各大商业网盘的安全性深受质疑，限速或者收费等限制并不能很好地满足我们对存储的需要。

总而言之：

1. 个人/家庭有使用分布式存储提高数据安全性的需求，同时有分散在若干个人设备的碎片化存储资源可以利用。
2. 传统的分布式文件系统并不适用于小型私有云场景。
3. 各种个人搭建私有云的方案可移植性较差，且部署需要较多配置。

面对前述现状，我们希望在前人的试验基础上，实现可用性更高的、可移植性更强的、基于互联网的小型分布式文件系统，在上述应用环境中提供比免费网盘更好的服务。

### 相关项目

在调研阶段我们了解了相关项目，我们从这些项目中获得了许多灵感，做出了我们自己的创新。以下是两类典型的系统。

#### IPFS

IPFS 全称 Interplanetary File System，意为星际文件系统，它创建了分布式存储和共享文件的网络传输协议，想要将所有具有相同文件系统的计算设备连接在一起。IPFS 有这些特性：

1. 使用区块链技术，使 IPFS 实现了文件存储的持久性，相比中心化的服务更难被篡改和封禁。
2. IPFS 在文件传输上是 P2P 的模式， 充分地利用了个节点之间的带宽资源，并且无需一个强大的中央服务器来满足大量服务，达到了高效和低成本。
3. 每一个上传到 IPFS 的文件会被分配一个内容的哈希作为地址，访问只需验证哈希。
4. Filecoin 是 IPFS上的一个代币，用来激励用户贡献闲置的硬盘。它通过算法确定工作量发放。简单的来说，拥有的硬盘容量越大，获取的 Filecoin 越多。

#### NAS

![image-20200720182514522](conclusion.assets/image-20200720182514522.png)

NAS 全称 network attached storage，是一种可以通过网络访问的专用数据存储服务器，它可以将分布、独立的数据进行整合，集中化管理，以便于对不同主机和应用服务器进行访问的技术。

举个实际应用的例子，NAS 可以用于更好地管理家庭数据，例如家中的各种电脑，平板，手机等设备，可以通过 NAS统一存储数据和同步数据。

#### 前人工作

我们还参考了 2017 年 OSH 课程中的一个大作业项目，也是一个[基于互联网的分布式文件系统](https://github.com/IngramWang/DFS_OSH2017_USTC)。但是该项目的数据传输是中心化的，所有文件都要经过中央服务器的中转，有很多不利的地方。

我们从中借鉴了一些 idea，例如 Java 编写客户端有利于存储节点跨平台，提供 web 文件管理界面，还有使用纠删码做冗余。同时也吸取了一些经验教训，例如服务器的配置显得比较繁杂，我们采用 docker 进行了封装简化配置。

#### 小结

![image-20200720182904536](conclusion.assets/image-20200720182904536.png)

在上述两种技术中，IPFS 本身在去中心化上做的很充分，同时，因为完全去除了中心化的元素，也有没解决的问题：

- 存储数据的安全：没有验证用户身份，任何人只要拿到文件的哈希值就能拿到文件。
- 数据可靠性：IPFS系统虽然有激励层，但是无法保证一份数据能够有**足够的备份**在系统中存储，这就可能导致数据丢失。

我们的项目不仅有 IPFS 的存储节点去中心化的优势，还能避免其两大问题。这都归功于设计中的目录节点，我们之后会详细介绍。

![image-20200720182915233](conclusion.assets/image-20200720182915233.png)

至于 NAS 的局限性在于使用专有设备，前期安装和设备成本较高。NAS的可扩展性也有一定的限制，因为单台 NAS 的扩容是有限的，增加另一台NAS设备非常容易，但是要想将两个 NAS 设备的存储空间**无缝合并**就不容易了。 

而在我们的项目中，存储节点只需要持续运行本项目的 java 跨平台程序，并且我们的可扩展性上不存在 NAS 的问题。

## 项目设计

### 项目结构

![image-20200718185006861](conclusion.assets/image-20200718185006861.png)

我们的项目从 IPFS 中吸取了 P2P 的思想，在数据的传输上实现了点对点。文件系统由一个目录节点（index）和若干存储节点（storage）组成。目录节点负责提供 web 管理界面，以及协调各存储节点的资源。这样的数据存储和传输方式能够有效节约带宽资源，避免传统 server/client 模式中中央服务器负载过重产生的瓶颈。（像我们参考的另一个大作业项目，就存在中央服务器数据传输瓶颈的问题。）同时，文件内容也不会经过目录节点，不用担心权力集中和监管的弊端。

因为目录节点的存在，我们能够对访问者进行身份验证，对数据的冗余备份进行协调，不会产生 IPFS 中的安全性问题和可靠性问题。

相对于 NAS 较差的扩展性，由于本项目各节点之间的连接基于互联网，这非常有利于存储节点的接入和用户群的扩展。有更多的用户参与成为存储节点也会进一步提高系统的稳定性和可用性。

![image-20200718220623911](conclusion.assets/image-20200718220623911.png)

考虑到易用性，我们将目录节点运行的服务用 docker 容器进行封装，一方面解决了适配不同环境的问题提高了兼容性，另一方面也使一键部署成为可能。

只要存储设备运行我们的 java 程序就可以作为存储节点接入分布式文件系统。而因为 JVM 虚拟机的跨平台兼容，凡是能运行 Java 的设备都可以成为存储节点，无论 Windows、Mac OS、Linux 都可以兼容。这意味着使用者可以将任意的闲置资源方便地贡献为存储结点，这或将大大提升未来私人部署网盘的占有率，更能避免对商业云存储的依赖。

同样是处于易用性的考虑，我们提供 web 界面进行文件管理操作，一方面可以避免安装客户端的麻烦，另一方面我们也利用了 web 的跨平台兼容性。

这样一来从部署目录节点、接入存储节点到管理文件，整个系统操作都相当便捷，非常有利于自行部署搭建服务。

### Docker 容器化服务端

#### 为什么要使用容器化技术

在生产环境上，传统的手工部署方法可能会出现下列事件：

- 你的 Linux 发行版很老，而你需要运行一个给新版本的 Linux 或者完全不同的 Linux 发行版设计的程序。
- 你和朋友一起设计一个大型程序，由于你们的运行环境不同，有时候在某台机器上正常运行的程序，在另一台机器上没法正常运行。
- 你希望在多台服务器上部署同一个项目，但是项目需要非常复杂的配置，一个一个配置服务器的成本非常大。

而容器化技术可以方便解决上述问题，容器可以把应用及其依赖项都将打包成一个可以复用的镜像并与其他进程环境隔离。

在运行环境、网络拓扑、安全策略和存储方案变化时，软件可能会显现一些出乎意料的问题；而容器使开发环境和运行环境统一。同时容器并不像虚拟机那样模拟全部硬件（这对于很多轻量型应用是小题大做），它只虚拟化了文件系统、网络、运行环境等，在核心本地运行指令，不需要任何专门的接口翻译和系统调用替换机制，减少了很多虚拟化开销。

使用容器技术很好地简化了目录节点的配置，同时还可以减少开发环境和部署环境不同带来的问题。

很多时候人们可能因为配置文档过于复杂或者因为环境问题配置失败，就放弃了一个项目。提供一键部署的方案，降低了部署的成本和学习门槛，非常有利于项目的推广。

#### Docker-Compose

Docker-Compose 是 Docker 官方用于定义和运行多容器的编排工具。

在我们项目的部署中，Docker-Compose 提高了项目的易用性，使用者可以轻松地部署目录节点。

docker-compose 的 scale 功能还支持创建多个实例进行负载均衡反向代理。这可以在我们想进行用户群的扩展时，轻松解决目录节点高并发的问题，并把处理能力分布在多台主机上。

<img src="conclusion.assets/%E5%9B%BE%E7%89%871.png" alt="图片1" style="zoom:50%;" />

本项目中，下面这段 docker-compose.yml 描述了 mytomcat、mymysql 和 myserver 这三个 Docker 容器的镜像、端口、依赖等信息。

```yaml
version: '2'
services:
  mytomcat:
    image: tomcat:7
    hostname: mytomcat
    container_name: mytomcat
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      - mymysql
    links:
      - mymysql
    environment:
      - Xmn384m
      - XX:MaxPermSize=128m
      - XX:+UseConcMarkSweepGC
      - XX:+DisableExplicitGC
      - XX:+UseParNewGC
    volumes:
      - "$PWD/WebContent:/usr/local/tomcat/webapps"
  mymysql:
    build: ./mysqlinit
    image: mymysql:test
    container_name: mymysql
    restart: always
    ports:
      - "3306:3306"
    command: [
      '--default-authentication-plugin=mysql_native_password',
      '--character-set-server=utf8mb4',
      '--collation-server=utf8mb4_general_ci'
    ]
    environment:
      MYSQL_ROOT_PASSWORD: 201314
      MYSQL_USER: 'root'
      MYSQL_PASS: '201314'
      serverTimezone: Asia/Shanghai
    volumes:
      - "$PWD/mysqldata:/var/lib/mysql"
  myserver:
    build: ./myserver
    image: myserver:test
    container_name: myserver
    restart: always
    depends_on:
      - mymysql
    links:
      - mymysql
    volumes:
      - "$PWD/WebContent:/usr/local/tomcat/webapps"
    ports:
      - "2333:2333"
```

### 前端美化

项目的前端 UI 完全采用 layui 的实现，在用户使用时表现整洁但又不简陋。

### 文件编解码

#### Reed-Solomon 编码

纠删码（Erasure Code）是一种编码技术。它通过计算将 n 份原始数据增加至 n+m 份数据，并能由其中的任意 n 份数据还原出原始数据，即可以容忍不多于 m 份的数据失效。纠删码主要应用在网络传输中，用以提高存储系统的可靠性。相比多副本复制而言，它能以更小的数据冗余度获得更高数据可靠性， 但编码方式较复杂，需要大量计算。

里德-所罗门码（Reed-Solomon codes，RS codes）是纠删码的一类，常被应用在分布式文件系统中，我们希望使用它来提升文件系统的可靠性。下面介绍它的编解码原理。

#### 编解码原理

##### 编码

RS 编码以 word 为编码和解码单位，大的数据块拆分到字长为 w（取值一般为 8 或者 16 位）的 word，然后对 word 进行编解码。数据块的编码原理与 word 编码原理相同。把输入数据视为向量 D = (D1, D2, .., Dn), 编码后数据视为向量 (D1, D2, .., Dn, C1, C2, .., Cm)，RS 编码可视为如下图所示矩阵运算。

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/research-RS-1)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/research-RS-1)

上图最左边是编码矩阵（或称为生成矩阵、分布矩阵，Distribution Matrix），编码矩阵需要满足任意 n\*n 子矩阵可逆。为方便数据存储，编码矩阵上部是单位阵，下部是 m\*n 矩阵。下部矩阵可以选择范德蒙德矩阵或柯西矩阵。

##### 解码

RS 最多能容忍 m 个数据块被删除，数据恢复的过程如下：

- 假设 D1、D4、C2 丢失，从编码矩阵中删掉丢失的数据块/编码块对应的行。根据 RS 编码运算等式，可以得到 B' 以及等式：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/research-RS-2-new)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/research-RS-2-new)

- 由于 B' 是可逆的，记 B' 的逆矩阵为 B'^(-1)，则 B'*B'^(-1) = I 单位矩阵。两边左乘 B' 逆矩阵：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/research-RS-4)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/research-RS-4)

- 得到如下原始数据 D 的计算公式，从而恢复原始数据 D：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/research-RS-5-new)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/research-RS-5-new)

#### 稳定性

我们采用纠删码技术对文件进行冗余。下面我们做个简单的计算，和简单副本备份进行对比。

假设单个存储节点的故障率为 p。采用副本备份的数量为 k，于是副本冗余的丢失概率为 p^k，就是 k 份全部丢失的概率。采用纠删码时将文件分为 n 块原始数据块，以及冗余 m 块校验块，并且 n+m 块分给不同的节点。纠删码的丢失概率可以用以下式子计算出，就是丢失碎片数量大于 m 块的概率。

![image-20200720181651317](conclusion.assets/image-20200720181651317.png)

代入数据，可以看到在故障率为1%，文件分 5 块并冗余 5 块条件下，纠删码的故障率仅有 2*10^(-10)，有效保障了数据安全不易丢失。而靠副本冗余想达到同样的效果，需要 5 份备份。

因此纠删码技术在同等丢失率条件下比副本备份节约了3倍于文件大小的空间，大幅减少了冗余所需的额外空间开销。

#### 编码矩阵

##### 基于范德蒙德（Vandermonde）矩阵

在线性代数中有一种矩阵称为范德蒙德矩阵，它的任意的子方阵均为可逆方阵。

一个 m 行 n 列的范德蒙德矩阵定义如下图左边，其中 Ai 均不相同，且不为 0。令 A1, A2, .., An 分别为 1, 2, 3, .., n，则得到范德蒙德矩阵为下图右边：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/feasibility-RS-Vandermonde-1)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/feasibility-RS-Vandermonde-1)

编码矩阵就是单位矩阵和范德蒙德矩阵的组合。输入数据 D 和编码矩阵的乘积就是编码后的数据。

采用这种方法的算法复杂度还是比较高的，编码复杂度为 O(mn)，其中 m 为校验数据个数，n 为输入数据个数。解码复杂度为 O(n^3)。

##### 基于柯西（Cauchy）矩阵

柯西矩阵的任意一个子方阵都是奇异矩阵，存在逆矩阵。而且柯西矩阵在迦罗华域上的求逆运算，可以在 O(n^2) 的运算复杂度内完成。使用柯西矩阵，比范德蒙德矩阵的优化主要有两点：

- 降低了矩阵求逆的运算复杂度。范德蒙矩阵求逆运算的复杂度为 O(n^3)，而柯西矩阵求逆运算的复杂度仅为 O(n^2)。
- 通过有限域转换，将 GF(2^w) 域中的元素转换成二进制矩阵，将乘法转换为逻辑与，降低了乘法运算复杂度。（二进制的加法即 XOR，乘法即 AND）

柯西矩阵的描述如下图左边，Xi 和 Yi 都是迦罗华域 GF(2^w) 中的元素。右边是基于柯西矩阵的编码矩阵：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/feasibility-RS-Cauchy-1-new.png)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/feasibility-RS-Cauchy-1-new.png)

##### 柯西编解码过程优化

在范德蒙编码的时候，我们可以采用对数/反对数表的方法，将乘法运算转换成了加法运算，并且在迦罗华域中，加法运算转换成了 XOR 运算。

柯西编解码为了降低乘法复杂度，采用了有限域上的元素都可以使用二进制矩阵表示的原理，将乘法运算转换成了迦罗华域“AND 运算”和“XOR 逻辑运算”，提高了编解码效率。

从数学的角度，在迦罗华有限域中，任何一个 GF(2^w) 域上的元素都可以映射到 GF(2) 二进制域，并且采用一个二进制矩阵的方式表示 GF(2^w) 中的元素。例如 GF(2^3) 域中的元素可以表示成 GF(2) 域中的二进制矩阵：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/feasibility-RS-GF-1)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/feasibility-RS-GF-1)

上图中，黑色方块表示逻辑 1，白色方块表示逻辑 0。通过这种转换，GF(2^w) 域中的阵列就可以转换成 GF(2) 域中的二进制阵列。生成矩阵的阵列转换表示如下：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/feasibility-RS-GF-2)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/feasibility-RS-GF-2)

在 GF(2^w) 域中的编码矩阵为 K*(K+m)，转换到 GF(2) 域中，使用二进制矩阵表示，编码矩阵变成了 wk*w(k+m) 二进制矩阵。采用域转换的目的是简化 GF(2^w) 域中的乘法运算。在 GF(2) 域中，乘法运算变成了逻辑与运算，加法运算变成了 XOR 运算，可以大大降低运算复杂度。

和范德蒙编解码中可能使用的对数/反对数方法相比，这种方法不需要构建对数或反对数表，可以支持 w 为很大的 GF 域空间。采用这种有限域转换的方法之后，柯西编码运算可以表示如下：

[![img](https://github.com/OSH-2020/x-dontpanic/raw/master/docs/files/feasibility-RS-GF-3)](https://github.com/OSH-2020/x-dontpanic/blob/master/docs/files/feasibility-RS-GF-3)

使用柯西矩阵要优于范德蒙德矩阵的方法，柯西矩阵的运算复杂度为 O(n*(n-m))，解码复杂度为 O(n^2)。

#### 开源纠删码项目

纠删码本身目前已经是一种比较成熟的算法，且其中的 Reed-Solomon 算法是比较早并且已经有开源实现的一种算法，相对引入系统的难度较低。此外，为了在浏览器端实现文件编解码以减少服务器的工作量，我们希望应用 WebAssembly 编译现有的开源算法。

我们分别尝试了用 JavaScript 和 WebAssembly 在浏览器上做纠删码。其中 JavaScript 直接基于[开源的实现](https://github.com/ianopolous/ErasureCodes)进行了一些修改。

数据储存服务供应商 Backblaze 在 GitHub 开源提供了一个使用 Java 编写的 Reed-Solomon 库。以此为基础实现了许多其他语言如 Go、Python 编写的 RS 纠删码项目，其中 [Go 语言的实现](https://github.com/klauspost/reedsolomon)有较多的 Star 量，内容也较为完善。为了在网页中应用项目中的函数，我们利用它编写了 Go 语言代码编译成 WebAssembly 格式，这部分内容在后面介绍。

#### WebAssembly

WebAssembly 是一个实验性的低级编程语言，应用于浏览器内的客户端。

在过去很长一段时间里，JavaScript 是 Web 开发人员中的通用语言。如果想写一个稳定成熟的 Web 应用程序，用 JavaScript 几乎是唯一的方法。WebAssembly（也称为 wasm）将很快改变这种情况。它是便携式的抽象语法树，被设计来提供比 JavaScript 更快速的编译及运行。

WebAssembly 将让开发者能运用自己熟悉的编程语言（最初以 C/C++ 作为实现目标）编译，再藉虚拟机引擎在浏览器内运行。

WebAssembly 的开发团队分别来自 Mozilla、Google、Microsoft、Apple，代表着四大网络浏览器 Firefox、Chrome、Microsoft Edge、Safari。2017 年 11 月，以上四个浏览器都开始实验性地支持 WebAssembly。WebAssembly 于 2019 年 12 月 5 日成为万维网联盟（W3C）的推荐，与 HTML、CSS 和 JavaScript 一起，成为 Web 的第四种语言。

#### WebAssembly 与 JavaScript 效率对比

在项目 demo 完成后，我们对 WebAssembly 和 JavaScript 代码的效率进行测试得到了两组对比的 benchmark，两组对文件大小的参数进行了更改。可以看到在 WebAssembly 上实现的纠删码效率远远高于 JavaScript，编码速率提升将近 4 倍，而解码提升了 7 倍左右。当有一块文件块缺失时，WebAssembly 的解码效率提升了 10 倍。

![image-20200719094351911](conclusion.assets/image-20200719094351911.png)

#### 浏览器端实现文件编解码

##### 使用 FileReader 获取本地文件

通过使用在 HTML5 中加入到 DOM 的 File API，用户可以在 web 内容中选择本地文件然后读取这些文件的内容。FileReader 对象允许Web 应用程序异步读取存储在用户计算机上的文件（或原始数据缓冲区）的内容，使用 File 或 Blob 对象指定要读取的文件或数据。

我们成功读取文件后，记录它的文件名、大小、设置的分块数等信息，另外需要将其转换为 Uint8Array 格式，这是因为在 Go 接收 JavaScript 传递的数据时，需要通过 CopyBytesToGo 方法拷贝数据到 Go 的对象中，这个方法要求传递 Uint8Array 类型的数据。在 JavaScript 版本的代码中，也要将原始文件内容转换为 Uint8Array 编码。

之后，我们创建 Worker 线程，在其中调用 JavaScript 或者由 Go-WebAssembly 导出的函数进行文件编码，并生成碎片的 MD5 摘要。

##### Go-WebAssembly：使用 syscall/js 包编写源代码

Go 提供了专有 API syscall/js 包，使我们可以与 JavaScript 之间传递函数与数据。来源于 JavaScript 的数据在 Go 中会是 js.Value 类型，需要使用包里的函数进行转换。除了通过上一节提到的 CopyBytesToGo 方法拷贝 JavaScript 数据到 Go 的对象中，我们还会用到 CopyBytesToJS 将运算结果返回给 JavaScript，以及 FuncOf 用于包装 Go 函数，等等。

在 Go 代码中，我们接收 Uint8Array 类型数据，并提供三个函数给 JavaScript 使用：

- callEncoder 用于编码；
- callDecoder 用于解码；
- callMd5 用于计算碎片的 MD5 值，从而在解码时判断碎片内容是否发生了改变。

这三个函数将会完成数据类型的转换和 Go 函数的调用。在 main() 函数中声明这些函数，并阻止 Go 程序退出。

```go
func main() {
	c := make(chan struct{}, 0)
	js.Global().Set("callMd5",js.FuncOf(callMd5))
	js.Global().Set("callEncoder",js.FuncOf(callEncoder))
	js.Global().Set("callDecoder",js.FuncOf(callDecoder))
	<-c
}
```

接下来分别简述 Go-WebAssembly 的三个函数。

##### callEncoder：接收原始数据并调用 Go 函数编码

为了能将 Go 函数传递给 JavaScript 使用，Go 函数的参数和返回值类型在[js 包文档](https://golang.org/pkg/syscall/js/#FuncOf)中有固定格式的要求。

```go
func FuncOf(fn func(this Value, args []Value) interface{}) Func
```

这意味着 JavaScript 和 Go 的数据需要经过一些转换。callEncoder 函数声明为：

```go
func callEncoder(this js.Value, args []js.Value) interface{}
```

而在 JavaScript 代码中调用 callEncoder 函数时，我们接收 JavaScript 中 Uint8Array 类型的原始文件数据，以及进行纠删码编码需要的原始数据块、冗余块两个参数，并传递给 goEncoder 以调用 Go 开源库的函数。

```go
buffer := make([]byte, args[0].Length())
js.CopyBytesToGo(buffer, args[0])
content := goEncoder(buffer, args[1].Int(), args[2].Int())
```

得到编码后的数组（content）后，再调用 CopyBytesToJS 函数转换成 js.Value 类型，于是函数的返回值能在 JavaScript 代码中直接使用。

```go
jsContent := make([]interface{},len(content))
for i:=0; i<len(content); i++{
    jsContent[i] = js.Global().Get("Uint8Array").New(len(content[0]))
    js.CopyBytesToJS(jsContent[i].(js.Value),content[i])
}
return js.ValueOf(jsContent)
```

在 goEncoder 函数中，我们可以直接使用[开源库](https://github.com/klauspost/reedsolomon)中的函数进行编码。为了在生成编码矩阵时使用性能上更好的柯西矩阵，参照[说明文档](https://pkg.go.dev/github.com/klauspost/reedsolomon?tab=doc#WithAutoGoroutines)加入 WithCauchyMatrix() 参数。编码得到的结果返回给 callEncoder 函数进行格式转换。

```go
func goEncoder(raw []byte, numOfDivision int, numOfAppend int)(content [][]byte){
	enc, err := reedsolomon.New(numOfDivision, numOfAppend, reedsolomon.WithCauchyMatrix())
	checkErr(err)
	content, err = enc.Split(raw)
	checkErr(err)
	err = enc.Encode(content)
	checkErr(err)
	return content
}
```

##### callMD5：为碎片生成 MD5 摘要

碎片的摘要可以用于检验碎片内容是否发生变化，从而在解码时忽略已经损坏的碎片。通过比较本地文件和云端文件的摘要也可以实现文件秒传功能，在我们的项目中还没有实现。

MD5 是一种被广泛使用的摘要算法，使用它可以为每个碎片产生一个128位（16字节）的哈希值。Go 的 crypto/md5 包提供了md5.Sum() 函数来进行这个运算。

在 callMD5 函数中，我们调用计算 MD5 的函数，并用 fmt.Sprintf() 函数将字符类型的运算结果直接返回给 JavaScript。

这一部分的代码思路借鉴了[使用Go开发前端应用（三）](https://juejin.im/post/5eb2191df265da7bbf21a0f4)。

```go
func callMd5(this js.Value, args []js.Value) interface{} {
	// 声明一个和文件大小一样的切片
	buffer := make([]byte, args[0].Length())
	// 将文件的bytes数据复制到切片中，这里传进来的是一个Uint8Array类型
	js.CopyBytesToGo(buffer, args[0])
	// 计算md5的值
	res := md5.Sum(buffer)
	// 调用js端的方法，将字符串返回给js端
	return fmt.Sprintf("%x", res)
}
```

##### callDecoder：接收碎片并调用 Go 函数解码

如果碎片的摘要发生变化，说明碎片可能损坏，在解码时应当认为碎片丢失。在 callDecoder 函数中，接收到的参数是 JavaScript 代码中由文件碎片组成的二维数组，其中我们会将摘要值不符合记录的碎片设为 null。

对于每一块碎片（在 Go 代码中它可以表示为 args[0].Index(i)）我们判断它是否为空，并转换成 Go 中的类型，然后进行解码。

```go
buffer := make([][]byte, args[0].Length())
for i:=0; i<len(buffer); i++ {
    // if args[0][i]==null, set buffer[i] as nil.
    if !args[0].Index(i).Equal(js.Null()) {
        buffer[i] = make([]byte, args[0].Index(i).Length())
        js.CopyBytesToGo(buffer[i], args[0].Index(i))
    }else {
        buffer[i]=nil;
    }
}
content := goDecoder(buffer, args[1].Int(), args[2].Int())
```

解码完成后，再进行类型转换返回给 JavaScript。

#### Go-WebAssembly 编码性能

![image-20200719102043457](conclusion.assets/image-20200719102043457.png)

这张图是两组不同的纠删码参数下，编码时间随文件大小的变化。可以看到两组都呈现编码时间随文件大小线性增长。在 40 + 20 这一组中，平均每 1MB 的文件需要消耗约 76ms 来编码，16 + 8 的时候是 33ms/MB。或者换一个角度看，编码的吞吐速率分别为大约 13MB 每秒和 30MB 每秒。这样的吞吐量已经相当不错了，与千兆网带宽在一个数量级上。

对于很大的文件，可以把文件先分成小块再逐一做纠删码。同时，分块还可以有其他好处。例如可以用不止一个 worker 并行地做纠删码，这样时间可以进一步缩短。

在此之上还能追加流水作业，如下图，负责编码的 worker 将编码完成的数据块交给上传的 worker 发送，他们的吞吐速率大致相同，形成流水线作业，这可以完全将编码带来的时间开销隐藏在传输时间当中。采用这样的方式，上传下载速度可以非常快，实现了高效。

![image-20200719102239251](conclusion.assets/image-20200719102239251.png)

### WebSocket

<img src="conclusion.assets/image-20200719102716707.png" alt="image-20200719102716707" style="zoom:50%;" />

JavaScript 没有可以直接使用的 TCP 接口。为了在浏览器和存储节点之间直接传输数据，我们选择了 WebSocket 协议来实现浏览器和客户端的直连。这样做的好处有：

- WebSocket 本身建立在 TCP 之上，做一个服务器端的实现比较容易。

- 它和 HTTP 有良好兼容性，在握手阶段采用 HTTP 发送一个特殊的请求头，要求升级到 WebSocket。并且它也使用 80/443 端口，因此它能顺利通过各种HTTP代理服务器。

- WebSocket 也是一个数据格式非常轻量的协议，在我们传输文件的场合无疑需要这样的高效通信。

我们根据相关文档在客户端加入处理 WebSocket 的 API。然后就具备了在用户，也就是浏览器端和存储节点之间直接建立数据连接的通信方式。

### 碎片分配策略

考虑到接入系统的存储节点可以是个人设备，不一定能24小时全天在线。为了保证下载文件的成功率，我们需要一个合理的碎片分配策略。

只需要加入一个考虑设备和用户在线时间重合度的分配策略，就可以大幅提高下载成功概率。

我们将一个设备或者一个用户一天中的在线时间表示成一个长度为 24 的 01 向量，在上传文件时尽可能地给覆盖上传者的在线时间段 x% 以上的存储结点分配碎片。

这里的 x% 的计算是设备与用户需求的距离。24个时间段内，有 x% 以上的时间段不会发生用户在线而设备不在线的情况。

我们的分配策略还估计到了剩余容量，碎片不会再分给剩余容量到达上限的节点，避免分配出现严重的倾斜。

相关代码如下：

```java
private DeviceItem[] getAllocateDeviceList(Query query,int nod,int noa, String whose){
	// 确认有在线设备
	DeviceItem[] onlineDevice = query.queryOnlineDevice();
	if(onlineDevice == null){
		return null;
	}
	// 计算相似度 0<=distance<=24
	int onlineDeviceNum = onlineDevice.length;
	int[] distance = new int[onlineDeviceNum];
	for(int i=0; i<onlineDeviceNum; i++){
		int save = query.queryUserTime(whose);
		int time = onlineDevice[i].getTime();
		distance[i] = 0;
		for(int j=0; j<24; j++){ // 24维
			if((time & 1) == 0 & (save & 1) == 1)
				distance[i]++;
			time = time >> 1;
			save = save >> 1;
		}
	}

	int fragmentSize = fileSize/nod;
	// 由于有 vlab，必然有至少一台distance <= 30% * 24 = 7
	ArrayList<Integer> distanceId = new ArrayList<>();
	for(int i=0; i<onlineDeviceNum; i++){
		if(distance[i]<=7 & onlineDevice[i].getLeftrs() > fragmentSize)
			// 差距够小 且 至少可以分配一个碎片
			distanceId.add(0, i); // 一直从头插入
	}
	int size = distanceId.size(); // 有效在线主机数
	if(size < 1)
		return null;
	// 根据碎片数量和有效在线主机数，确定结果
	DeviceItem[] deviceItemList=new DeviceItem[nod+noa];
	if(noa+nod <= size){
		for(int i=0;i<nod+noa;i++){
			deviceItemList[i] = onlineDevice[distanceId.get(i)];
			deviceItemList[i].setLeftrs(deviceItemList[i].getLeftrs() - fragmentSize);
		}
	}
	else{ // noa+nod > size
		int i = noa+nod-1;
		int j = 0;
		while(i>=0){
			DeviceItem thisdevice = onlineDevice[distanceId.get(j)];
			if(thisdevice.getLeftrs() > fragmentSize){
				deviceItemList[i] = thisdevice;
				thisdevice.setLeftrs(thisdevice.getLeftrs() - fragmentSize);
				query.alterDevice(thisdevice);
				i--;
			}
			j = (j+1)%size;
		}
	}
	return deviceItemList;
}
```

接下来，通过数学公式和计算图表来展示分配策略的效果。

我们设上传文件是在线存储节点数目为n，文件分为nod块碎片，冗余noa块碎片，在线率为p，则取得完整文件的成功率表达式如下：
$$
\sum_{i= \left \lceil n\times nod{\div} \left ( nod+noa \right )  \right \rceil }^{n} \binom{n}{i}\times p^{i}\times (1-p)^{n-i}
$$

假设每一个拿到碎片的设备在线率都为 p = 70%，设备为 n 台，取纠删码分 nod = 4 块碎片，冗余 noa = 4 块碎片的参数，代入图中公式，随着 n 的增大得到接近 100% 的成功率。

![n个设备对应成功率折线图](conclusion.assets/n个设备对应成功率折线图.png)

同时，我们还可以加入一些保证 24h 在线的可靠节点（例如商业云等稳定的云服务）作为一个单独的分类，此时分配策略可以固定向可靠节点分一定比例的碎片，那么那些碎片的在线率可以视为 100%，进一步提高下载成功概率。

### 多用户权限支持

#### 前置项目关于用户权限的设计

##### 数据库配置

服务器数据库模块负责分布式文件系统的数据库访问，包括封装了数据库访问方法的 Query 类与用于定义数据结构的 FileItem、DeviceItem、RequestItem 类。

本分布式文件系统使用数据库维护所有的元数据，数据库中具体包括表 FILE 用于存储文件的逻辑位置与属性、表 FRAGMENT 用于存储碎片的物理位置、表 REQUEST 用于存储服务器对客户端的碎片请求、表 DEVICE 用于存储系统中客户端的信息、表 USER 用于存储网页的注册用户。

```sql
CREATE TABLE `DEVICE` (
`ID` int NOT NULL AUTO_INCREMENT,
`IP` char(20) NOT NULL DEFAULT '',
`PORT` int NOT NULL DEFAULT 0,
`ISONLINE` boolean NOT NULL,
`RS` int NULL DEFAULT 0 ,
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FRAGMENT` (
`ID` int NOT NULL,
`PATH` char(20) NOT NULL DEFAULT '',
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FILE` (
`ID` int NOT NULL AUTO_INCREMENT,
`NAME` char(20) NOT NULL DEFAULT '',
`PATH` char(60) NOT NULL DEFAULT '',
`ATTRIBUTE` char(10) NOT NULL DEFAULT '',
`TIME` char(10) NOT NULL DEFAULT '',
`NOA` int NOT NULL DEFAULT 1,
`ISFOLDER` boolean NOT NULL DEFAULT false,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `REQUEST` (
`ID` int NOT NULL AUTO_INCREMENT,
`TYPE` int NOT NULL DEFAULT 0,
`FRAGMENTID` int NOT NULL DEFAULT 0,
`DEVICEID` int NOT NULL DEFAULT 0,
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `USER` (
`ID` int NOT NULL AUTO_INCREMENT,
`NAME` char(20) NOT NULL UNIQUE DEFAULT '',
`PASSWD` char(20) NOT NULL DEFAULT '',
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX `idx_FILE_PATH_NAME` ON `DFS`.`FILE` (PATH, NAME)
COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
CREATE UNIQUE INDEX `idx_USER_NAME` ON `DFS`.`USER` (NAME) COMMENT ''
ALGORITHM DEFAULT LOCK DEFAULT;
```

Query 类定义了对上述五个表查询、修改、删除、新增条目的函数，其通过 JDBC 接口实现了对数据的访问，访问的流程为：

（一）在构造函数中使用 DriverManager.getConnection 函数创建到数据库的连接（一个Connection 类实例）；

（二）通过 Connection 类实例的 createStatement 函数创建一个 Statement 类实例；

（三）通过 Statement 类实例的 executeQuery 函数执行 SQL，SQL 的内容可以使用格式化字符串根据函数的参数填入不同的内容，该函数将返回一个 ResultSet 类实例；

（四）对 ResultSet 类实例，使用 next 函数与 getInt、getBoolean、getString 等函数遍历查询的每个结果；

（五）对 ResultSet 类实例与 Statement 类实例，执行 close 函数关闭连接；

（六）在 closeConnection 函数中，调用 Connection 类实例 close 函数关闭连接。

#### 改进用到的技术

##### 新的数据库设计

利用 `FILE` 表的设计，加入 `WHOSE` 列，使得文件有了归属。

```sql
CREATE TABLE `FILE` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `NAME` char(100) NOT NULL DEFAULT '',
  `PATH` char(60) NOT NULL DEFAULT '',
  `ATTRIBUTE` char(10) NOT NULL DEFAULT 'rwxrwxrwx',
  `TIME` char(10) NOT NULL DEFAULT '',
  `NOD` int NOT NULL DEFAULT 1,
  `NOA` int NOT NULL DEFAULT 0,
  `ISFOLDER` boolean NOT NULL DEFAULT false,
  `WHOSE` char(20) NOT NULL DEFAULT '',
  `FILETYPE` char(50) NOT NULL DEFAULT '',
  `FILESIZE` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

##### 新的 Web 端设计

登录时，输入用户名、密码，检测对应模块，跳转至特定文件空间（涉及到由抓取所有文件列表到抓取特定文件列表的转变）；

核心是 `sql` 抓取语句的改进：

```java
public FileItem[] queryFileList(String whose, String path){
	Statement stmt = null;
	ResultSet rs = null;
	FileItem fileArray[] = null;

	int id, noa,nod;
	String name,attr, time;
	boolean isFolder;

	int count,i;

	try{
		stmt = conn.createStatement();
		String sql;
		sql = "SELECT * FROM DFS.FILE WHERE WHOSE='"+whose+"' AND PATH='"+path+"'";
		rs = stmt.executeQuery(sql);
		if (!rs.last())
			return null;
		count = rs.getRow();
		fileArray=new FileItem[count];
		i=0;
		rs.first();

		while (i<count) {
			id = rs.getInt("ID");
			nod = rs.getInt("NOD");
			noa = rs.getInt("NOA");
			name = rs.getString("NAME");
			attr = rs.getString("ATTRIBUTE");
			time = rs.getString("TIME");
			isFolder = rs.getBoolean("ISFOLDER");
			String fileType=rs.getString("FILETYPE");
			int fileSize=rs.getInt("FILESIZE");

			fileArray[i]=new FileItem(id,name,path,attr,time,nod,noa,isFolder,fileType,fileSize,whose);
			rs.next();
			i++;
		}
	}
	catch(Exception e){
		e.printStackTrace();
	}
	finally{
		try{
			if(rs!=null && !rs.isClosed())
				rs.close();
		}
		catch(Exception e){
		}
		try{
			if(stmt!=null && !stmt.isClosed())
				stmt.close();
		}
		catch(Exception e){
		}
	}
	return fileArray;
}
```

## 未来工作展望

在实现这个项目的过程中，我们也发现了一些值得思考的有趣问题。

### 重新加入中心节点

去中心化这件事其实是很值得权衡的，不同程度的去中心化各有其特性。例如 IPFS 去中心化比较彻底，但也留下了验证一类的问题。我们的项目决定保留目录节点，目录节点不承担文件数据的传输。其实如果再偏向中心化一点，给目录节点增加一些职能，让它也可以承担文件中转工作，也是另一种有趣的设计。

- 预约离线下载：当前我们的项目下载文件时只能收集瞬时在线的碎片，但是进行离线下载的话，目录节点可以帮我在一段连续的时间内收集碎片，这样一来成功率会进一步提升，并且受设备在线时间的影响被降低了。
- 帮助低性能设备编解码：用户某些设备配置较低，难以完成文件编解码的任务，这个时候可以要求目录节点代为执行编解码。

### 更高性能的纠删码模块设计

现有的 WASM 纠删码模块是把文件当成一个大数进行切割放入 RS 矩阵进行运算，性能较低下且占用资源较大。

我们可以把上次下载的文件以流的方式处理，以每 5 bit 为单位放入 RS 矩阵中进行运算，对文件进行分块处理，可以大幅度减少内存等资源的占用。

## 项目总结

TODO：项目做了哪些事情。

我们的 prototype 只是阶段性产出，但已经能看出它和 NAS、IPFS 的互补性以及独有的优势。NAS 不容易做到的高普及度，我们的却能做到了平台和设备无关性的实现，和硬件很好地隔离。

云时代的到来本身就对：专有硬件、专用存储软件，专业的人员来管理，这种一套昂贵存储方案产生了强烈的冲击。2020 年突如其来的疫情让许多人将生活和工作转到了线上，这更是大幅提升了云存储的需求度，也考验了基础设施在扩容上的灵活性。新的时代将属于由软件定义的存储。我们的项目在通用性和弹性上与在商业云存储有同样的优势，在商业云之外提供了另一种可能。可以构建一套掌控在自己手中，不依赖云服务商的中心化服务的分布式存储。

（TODO 展望：从这一块可以展望一下项目的价值，或者未来和分布式存储之间的趋势）

我们对目录节点做的容器化，为日后扩大用户群提高服务质量铺好了道路。这个架构意味着扩容能力、扩展性非常强，性能、稳定性很好。因为像之前提到，多个 docker 可以进一步地拆分到多台服务器，还可以启动多个实例提供更加高性能的服务。同时因为每一部分服务都是用 docker 隔离开的，哪一部分出问题了，重启那一部分一下就能解决。

相比每一个用户都要安装一个 APP，使用浏览器（它本身也是跨平台）能使用户感到非常方便。一个跨平台的客户端使得，用户可以任意将自己身边的设备变成存储节点。

目录节点的 Docker-compose，用户端的浏览器，存储节点的JVM虚拟机，每一部分都是一个便捷且兼容的方案，对于我们的项目来说恰到好处。

e.g.本小组的项目选题接受了邢凯老师的建议，使用一个全新的、行业内领先而资料并不齐备的硬件——可编程智能网卡——来尝试对冯诺依曼体系结构处理数据流的过程进行非冯化改造。项目过程中几经波折，接触了诸多曾经生疏的环境配置，包括网卡驱动、PROX 等等，感触颇深，收获不菲。

verifier 是 eBPF 程序硬件卸载的最大障碍，而 `clang` 等编译器与 verifier 不配合是导致这一障碍的最直接原因。智能网卡指令集与架构本身和传统 CPU 的差异与局限，包括指令集支持不充分，map helper function 支持不充分等，则是使得诸多能够挂载到内核层的 eBPF 程序无法通过网卡检查的根本原因。

面对 eBPF 体系结构本身的巨大局限，实现诸如 AlexNet 一类的复杂算法结构必须使用像 Netronome SDK 这种更高级、更了解网卡特性、更能充分利用网卡硬件资源的编程工具。由于本小组学期时间紧张，最终调研 AlexNet 只有三个人三周左右的时间，而 SDK 功能十分强大，网卡硬件资源繁多，文档内容全面，使得充分学习与利用之比较耗费时间。

但最终，本小组还是在软件资源下完成了方差分析算法的 eBPF 实现，并且对其延迟的估计也近似为纳秒级别。尽管最终 PROX 和 `rdtscp` 两种延迟测算方式或者无法配置，或者无法给出可信结果，但本小组的程序指令数已经很短，对延迟级别的估计也是建立在多次 `rdtscp` 测算的数量级观察上的，故可信度应当足够。

只是，虽然我们向 Netronome 官方获得了 SDK 授权，但未能继续 AlexNet 实现，实属可惜。但最终本小组在本学期课题上完成了基本任务，并在各个环境配置上、算法实现上等等环节积累了充分经验，可以为后来者做好铺垫了。

## 组员总结

TODO：总结各成员在项目中做的东西。可以大吹。最后加一段总结报告的分块编写。

组员袁一玮对项目的环境进行了 Docker 化封装，把其他组员的工作打包在一起，方便大家的测试和展示，节约了很多测试时不必要的步骤。把前端页面统一为 layui 的实现，使界面相比往届项目更清爽。

组员邱子悦对用户权限进行隔离，通过cookie验证，并尝试 CI 自动部署每次修改后的项目，并设计和实现了高效的碎片分配策略。同时配合组长一起鞭策鸽子（划掉），推进工作。



e.g.组长赵家兴全程与 Netronome 官方，与邢凯老师保持交流，解决诸如 eBPF 编译器、测时效果等问题。同时在整个项目的各个环节梳理项目框架，制定项目流程与规划，并组织组员讨论与分派任务。在项目面临巨大困境的时期解决了困难，并最终完成两个 eBPF 汇编代码的编写。

组员陶柯宇为全组完成了在服务器、虚拟机、远程链接等一切环境的配置，为其他组员专注于项目实现上极大地节约了时间。尝试了 PROX 的配置并给出不可行理由，帮助 PROX 开发者修正了编译的 bug，完成了方差分析项目的外部程序和使用 `rdtscp` 的测时程序。

组员付佳伟与陶柯宇一道帮助完成了环境配置，并对 SSH 设置反代方便组员连接，在数据包的发送与接收方法上做了诸多尝试并最终选择了适当方法。同时在 Linux 使用上给了组员相当大的帮助。没有陶柯宇和付佳伟二人对 Linux 优秀的能力水平，本小组的课程进度绝不可能有现在的程度。

组员李喆昊与陈昂合作，十分优秀地完成了 AlexNet 的调研、简化设计与 SDK 文档的阅读和硬件对应设计。本小组课题的深度、广度和推进程度极大程度上归功于二人的努力。二人还在调研报告和可行性报告上做出了主要贡献。

本报告的*“基于方差分析算法的状态切分 eBPF 实现”*部分，除*“外部包准备、发送、接收、测时、验证程序设计”*由陶柯宇编写，赵家兴修改并整合以外，其余部分由赵家兴编写；*“AlexNet 在智能网卡上实现的调研”*部分由陈昂和李喆昊共同编写，赵家兴修改并整合。整个报告由赵家兴主笔与整合，由陶柯宇校对。

![image-20200720180707335](conclusion.assets/image-20200720180707335.png)

## 致谢

邢凯老师参与了本小组选题与项目设计各个阶段的大量讨论，并在浏览器端实现纠删码、TODO、小组合作等方面为我们提供了许多建议与无私帮助。

陶柯宇等助教对本项目所采用技术工具给出了很多优质的建议。

学长。

在此本小组成员一并表示感谢。

## 参考文献

1. [容器化技术与 Docker](https://www.jianshu.com/p/34efcaa92ae4)
2. [Linux 101](https://101.ustclug.org/Ch08/)
3. [Erasure Code - EC纠删码原理](https://blog.csdn.net/shelldon/article/details/54144730)
4. [P2P 网络原理](https://www.cnblogs.com/ygjzs/p/12419548.html)
5. [P2P 技术原理](https://www.oschina.net/question/54100_2285064)
6. [github.com/peer44/java-rbac](https://github.com/peer44/java-rbac)
7. [Backblaze Reed-Solomon](https://www.backblaze.com/open-source-reed-solomon.html)
8. [github.com/klauspost/reedsolomon](https://github.com/klauspost/reedsolomon)
9. [译 Go和WebAssembly：在浏览器中运行Go程序](https://www.qichengzx.com/2019/01/01/go-in-the-browser.html)
10. [WebAssembly](https://zh.wikipedia.org/wiki/WebAssembly)
11. [基于 Token 的身份验证：JSON Web Token](https://ninghao.net/blog/2834)
12. [OpenVPN 的工作原理](http://blog.sina.com.cn/s/blog_6d51d1b70101cs5m.html)