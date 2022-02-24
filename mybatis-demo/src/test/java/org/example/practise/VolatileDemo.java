package org.example.practise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * java内存JMM模型解析可见性问题，JMM线程之通讯共享模型。
 * JVM storeLoad/...
 * jvm volatile 修饰变量底层解释成lock前缀指令
 */
public class VolatileDemo {

    private boolean flag=true;
    private int count=0;

    public void setFlag(){
        flag=false;
        System.out.println(Thread.currentThread().getName()+" changed the flag."+flag);
    }

    public void start(){
        System.out.println(Thread.currentThread().getName() + "开始执行.....");
        while (flag){
            // 1. flag,count没有加任何修饰，并能不结束。
            count++;
            // 2. 通过unsafe对象调用storeFence()方法，内存屏障，能结束线程。
            //UnsafeFactory.getUnsafe().storeFence();
            //3.释放CPU时间片，上下文切换，主程序加载最新值，能结束线程
            //Thread.yield();
            //4.内部有synchronized关键字，底层还是调用内存屏障能结束线程
            //System.out.println(count);
            //5.唤醒当前线程，内存屏障
            //LockSupport.unpark(Thread.currentThread());

            // 6.volatile修饰flag变量，可以结束
            // 7.volatile修饰count变量，可以结束
            // 8.count 使用Integer,final关键字修饰value值保证可见性。

            //9.sleep内存屏障,sleep调用操作系统代码fence()方法
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            //总结：  Java中可见性如何保证？ 方式归类有两种：
            //1.  jvm层面 storeLoad内存屏障    ===>  x86   lock替代了mfence
            // 2.  上下文切换   Thread.yield();

        }
        System.out.println(Thread.currentThread().getName() + "开始执行.....");
    }

    public void shortWait(long interval) {
        long start = System.nanoTime();
        long end;
        do {
            end = System.nanoTime();
        } while (start + interval >= end);
    }

    public static void main(String[] args) {

        VolatileDemo demo=new VolatileDemo();
        Thread threadA=new Thread(()->{
            demo.start();
        },"ThreadA");

        threadA.start();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread threadB=new Thread(()->{
            demo.setFlag();
        },"ThreadB");

        threadB.start();
    }

}
