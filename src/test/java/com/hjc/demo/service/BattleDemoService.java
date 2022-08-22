package com.hjc.demo.service;

import com.hjc.lua.LuaBattleManager;
import com.hjc.lua.LuaInit;
import com.hjc.lua.exception.LuaException;
import com.hjc.util.NamedThreadFactory;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * lua-java-battle战斗框架使用示例
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/8/18 21:27
 **/
public class BattleDemoService {

    private final Logger logger = LoggerFactory.getLogger(BattleDemoService.class);

    private static final BattleDemoService INSTANCE = new BattleDemoService();

    private BattleDemoService() {
    }

    public static BattleDemoService getInstance() {
        return INSTANCE;
    }

    /**
     * 战斗id自增
     */
    AtomicInteger battleIdCounter = new AtomicInteger(0);
    /**
     * lua战斗管理器
     */
    LuaBattleManager luaBattleManager = LuaBattleManager.getInstance();
    /**
     * 战斗核心map
     */
    private final Map<Integer, LuaTable> fightCoreMap = new ConcurrentHashMap<>();

    /**
     * 是否运行
     */
    private volatile boolean run;

    public static void main(String[] args) throws LuaException, InterruptedException {
        BattleDemoService battleDemoService = new BattleDemoService();
        battleDemoService.init();
        battleDemoService.start();
    }

    private void init() throws LuaException {
        LuaInit.LuaInitBuilder luaInit = LuaInit.builder();
        luaInit.preScript("print('Hello Lua Battle!!!')");
        // 设置lua根路径
        luaInit.luaRootPath("F:\\project\\lua-java-battle\\src\\main\\lua\\");
        // 加载lua调用接口目录
        luaInit.luaLoadDirectories("interface");
        // 加载lua主文件
        luaInit.luaLoadFiles("FightManager.lua");
        // 展示log
        luaInit.showLog(true);
        // 初始化全局lua环境
        luaBattleManager.init(luaInit.build());

        // lua主函数，可执行初始化等操作
        LuaFunction mainFunction = this.initFunction("Main");
        // 执行主函数，初始lua环境
        mainFunction.invoke();

        // 初始所有需要用到的方法
        this.updateFunction = this.initFunction("LOOPER.ServerUpdate");
        this.receiveMsgFunction = this.initFunction("COMMAND.ReceiveMsgTable");
        this.createFightCoreFunction = this.initFunction("CreateFightCore");
        this.initFightCoreFunction = this.initFunction("InitFightCore");
        this.closeFunction = this.initFunction("Close");

        // 创建10根线程，模拟游戏业务的线程模型
        for (int i = 0; i < 10; i++) {
            this.es[i] = Executors.newSingleThreadExecutor(new NamedThreadFactory("Battle-Looper-" + i));
        }
        // 单线程模拟网络消息的接收
        this.netExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Net-IO"));

        this.run = true;
        logger.info("luaBattleManager init");
    }

    // lua每帧驱动方法
    private LuaFunction updateFunction;
    // lua接收消息方法
    private LuaFunction receiveMsgFunction;
    // lua创建战斗核心方法
    private LuaFunction createFightCoreFunction;
    // lua初始战斗核心方法
    private LuaFunction initFightCoreFunction;
    // lua关闭战斗核心方法
    private LuaFunction closeFunction;


    // 创建10根线程，模拟游戏业务的线程模型
    private final ExecutorService[] es = new ExecutorService[10];
    // 单线程模拟网络消息的接收
    private ExecutorService netExecutor;

    private LuaFunction initFunction(String functionName) throws LuaException {
        LuaFunction function = luaBattleManager.initExecuteFunction(functionName);
        if (function == null || function.isnil()) {
            throw new LuaException(String.format("init.function.err - [%s].not.found", functionName));
        }
        logger.info(String.format("init.function.[%s]", functionName));
        return function;
    }

    /**
     * 创建战斗
     */
    public void createFightCore() {
        int battleId = battleIdCounter.getAndIncrement();
        // 创建战斗核心
        LuaTable fightCore = (LuaTable) this.createFightCoreFunction.invoke(LuaNumber.valueOf(battleId));
        this.fightCoreMap.put(battleId, fightCore);
        // 初始化战斗核心
        this.initFightCoreFunction.invoke(fightCore);
    }

    public void start() throws InterruptedException {
        // 模拟战斗场数
        int battleNum = 1000;
        // 模拟执行update次数
        int updateNum = 100;

        System.out.println(String.format("start.create.fightCore.for.%d.count", battleNum));

        for (int i = 0; i < battleNum; i++) {
            this.createFightCore();
        }

        // 接收网络消息
        receiveNetMsg();
        // 执行update
        executeUpdate(updateNum);

        // 退出程序
        System.exit(0);
    }

    /**
     * 模拟接收网络消息
     */
    private void receiveNetMsg() {
        this.netExecutor.submit(() -> {
            while (this.run) {
                for (Map.Entry<Integer, LuaTable> entry : this.fightCoreMap.entrySet()) {
                    // 模拟消息体和id号
                    int battleId = entry.getKey();
                    LuaTable fightCore = entry.getValue();
                    LuaTable luaTable = new LuaTable();
                    luaTable.set("testKey", "testValue");
                    this.receiveMsgFunction.invoke(new LuaValue[]{fightCore, LuaNumber.valueOf(battleId), LuaNumber.valueOf(battleId), luaTable});
                }
                // 每1s收到一条消息并调用lua进行接收
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        });
    }

    /**
     * 模拟执行每帧update
     *
     * @param updateNum
     * @throws InterruptedException
     */
    private void executeUpdate(int updateNum) throws InterruptedException {
        // 计数器，让每一场战斗执行num次update
        CountDownLatch countDownLatch = new CountDownLatch(updateNum * this.fightCoreMap.size());

        long start = System.currentTimeMillis();
        System.out.println(String.format("start.update.for.%d.count", updateNum));

        for (int i = 0; i < updateNum; i++) {
            for (Map.Entry<Integer, LuaTable> entry : this.fightCoreMap.entrySet()) {
                int battleId = entry.getKey();
                LuaTable fightCore = entry.getValue();

                int index = battleId % this.es.length;
                this.es[index].submit(() -> {
                    // 执行update方法
                    this.updateFunction.invoke(fightCore);

                    // 可以模拟每帧运行时间
                    /*
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                    }
                    */

                    countDownLatch.countDown();
                });
            }
        }

        System.out.println("start.await");
        countDownLatch.await();

        // 关闭
        System.out.println("start.close.invoke");
        for (LuaTable fightCore : this.fightCoreMap.values()) {
            this.closeFunction.invoke(fightCore);
        }

        // 战斗关闭后，从全局map移除
        this.fightCoreMap.clear();

        // 关闭
        this.run = false;

        for (ExecutorService e : this.es) {
            e.shutdownNow();
        }
        this.netExecutor.shutdownNow();

        long end = System.currentTimeMillis();
        System.out.println("time : " + (end - start) + "ms");
    }

    /**
     * 获取战斗核心
     *
     * @param battleId 战斗id
     * @return 战斗核心
     */
    public LuaTable getFightCore(int battleId) {
        return this.fightCoreMap.get(battleId);
    }
}
