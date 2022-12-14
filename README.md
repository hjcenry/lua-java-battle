# Java-Lua 战斗框架

![Powered][1]

[1]: https://raw.githubusercontent.com/github/explore/80688e429a7d4ef2fca1e82350fe8e3517d3494d/topics/lua/lua.png

[README in english](https://github.com/hjcenry/lua-java-battle/blob/master/README.en.md)

> **基于luaj实现的java使用lua的战斗框架**

该框架基于luaj的二次封装实现（https://luaj.sourceforge.net）

# 主要提供以下功能

- luaj基础接口的调用封装
- 简化luaj环境搭建步骤
- 管理lua战斗并提供接口
- lua面向对象使用方案(class.lua)
- lua战斗框架[使用示例](https://github.com/hjcenry/lua-java-battle/src/main/test/com/hjc/demo/service/BattleDemoService.java)
- luaj踩坑指南

该框架提供Java-Lua的战斗框架，有以下优缺点

- **优点**：

1. 公用逻辑lua代码：前后端可以基于同一套语言使用同一套战斗逻辑代码，只需要设计好共战框架，即可实现一份代码两处使用。前后端程序员也可以基于这套框架共同开发，这无论是对于状态同步还是帧同步来说，都可以一定程度提升开发效率。
2. luaj框架相比其他java调用lua方式，是目前位置效率最高的。
3. lua代码无需编译即可直接使用，可以通过luaj设计一套热更逻辑

- **缺点**（**踩坑指南**）：

1. 占用jvm更多的堆和meta空间

luaj提供两种编译器，luac和luajc。其中luac在load文件之后，会创建一个LuaClosure对象，其运行过程中会逐行解析lua命令，当然其运行效率也不会太高。
而luajc的原理是通过编译成Java字节码，并通过它的JavaLoader（继承ClassLoader）加载到内存，相当于一次编译多次运行。
但是熟悉的Java类加载机制的朋友应该清楚，这个过程中，JVM会在meta空间创建Klass信息，并在ClassLoader保存Klass引用。与此同时，luaj的JavaLoader也做了一件事：缓存动态生成的字节码byte[]
这种情况下，启动一个lua环境则会增加JVM的堆和meta空间的占用。

2. 运行效率不如原生Java

luaj的作者描述luaj的运行效率基本和原生lua虚拟机运行效率相当，甚至反超。但在我的实际测试中，我没有拿luaj和原生lua对比，而是拿luaj和原生java相比，其性能是远不如原生Java的。
通过观察luaj编译后的源码也能发现，拿i++这样一个操作来举例，原本在Java中，是可以直接对基本数据类型int进行操作的，而在luaj中，会对int进行类似Integer的包装类（LuaInteger）进行包装。

- 既是优点也是缺点：
1. 灵活的lua代码

灵活是一把双刃剑，用好了可以大幅提升开发效率，而用的不好的话，不仅不能提升开发效率，还可能对开发和维护，都带来极大的痛苦，这非常考研底层开发的能力。

> **综上所述**：是否使用lua作为Java服务器的战斗逻辑代码，需要根据实际情况而定，它的优点是否给你代码巨大好处，同时你也能忍受它的缺点或者有其他方案克服它的缺点。

`欢迎大家使用，有任何bug以及优化需求，欢迎提issue讨论`

# Java Doc

https://hjcenry.com/lua-java-battle/doc/

# 快速开始

完整代码示例可以参考BattleDemoService

# maven地址

```xml
<dependency>
    <groupId>io.github.hjcenry</groupId>
    <artifactId>lua-java-battle</artifactId>
    <version>1.0</version>
</dependency>
```

## 1. 指定Lua参数

```java
LuaInit.LuaInitBuilder luaInit=LuaInit.builder();
luaInit.preScript("print('Hello Lua Battle!!!')");
// 设置lua根路径
luaInit.luaRootPath("F:\\project\\lua-java-battle\\src\\main\\lua\\");
// 加载lua调用接口目录
luaInit.luaLoadDirectories("interface");
// 加载lua主文件
luaInit.luaLoadFiles("FightManager.lua");
// 展示log
luaInit.showLog(true);
```

## 2. 初始化Lua环境

```java
LuaBattleManager.getInstance().init(luaInit.build());
```

## 3. 初始化并缓存Java调用的Lua方法

```java
// 初始所有需要用到的方法
this.xxxFunction=this.initFunction("XXX.xxx");
this.xxxFunction2=this.initFunction("xxx");
```

## 4. 调用Lua方法

```java
this.xxxFunction.invoke();
this.xxxFunction2.invoke(LuaNumber.valueOf(123));
```

> `以上是简单的示例这个框架应该如何使用，BattleDemoService中提供了一套比较完成Lua战斗框架的示例`

# 使用方法以及例子

1. [战斗Service示例](https://github.com/hjcenry/lua-java-battle/src/main/test/com/hjc/demo/service/BattleDemoService.java)
2. [Lua-Java数据转换工具使用示例](https://github.com/hjcenry/lua-java-battle/src/main/test/com/hjc/demo/conver/ConvertModelLuaFileTest.java)
3. [Lua-Java库转换工具使用示例](https://github.com/hjcenry/lua-java-battle/src/main/test/com/hjc/demo/conver/ConvertLibLuaFileTest.java)

# 相关资料

1. https://www.lua.org/ lua官网
2. https://luaj.sourceforge.net luaj官网

# 交流

- 微信:hjcenry