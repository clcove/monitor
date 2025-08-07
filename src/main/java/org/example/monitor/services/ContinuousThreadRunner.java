package org.example.monitor.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ContinuousThreadRunner implements CommandLineRunner {

    private volatile boolean running = true;

    @Override
    public void run(String... args) {
        Thread continuousThread = new Thread(() -> {
            while (running) {
                try {
                    // 你的业务逻辑
                    System.out.println("持续运行的线程正在工作 - " + System.currentTimeMillis());
                    SystemInfo systemInfo = new SystemInfo();
                    HardwareAbstractionLayer hardware = systemInfo.getHardware();
                    //cpu占用率
                    int cpuUsage = 0;
                    CentralProcessor centralProcessor = hardware.getProcessor();
                    long[] prevTicksArray = centralProcessor.getSystemCpuLoadTicks();
                    long prevTotalTicks = Arrays.stream(prevTicksArray).sum();
                    long prevIdleTicks = prevTicksArray[CentralProcessor.TickType.IDLE.getIndex()];

                    Util.sleep(1000);

                    long[] currTicksArray = centralProcessor.getSystemCpuLoadTicks();
                    long currTotalTicks = Arrays.stream(currTicksArray).sum();
                    long currIdleTicks = currTicksArray[CentralProcessor.TickType.IDLE.getIndex()];

                    long idleTicksDelta = currIdleTicks - prevIdleTicks;
                    long totalTicksDelta = currTotalTicks - prevTotalTicks;

                    if (!(totalTicksDelta == 0)) {
                        cpuUsage = (int) ((1 - (double) idleTicksDelta / totalTicksDelta) * 100);
                    }
                    GlobalCache.put("cpuUsage", cpuUsage);

                    // 获取硬盘IO信息
                    Map<String,Map<String, String>> hardDiskIostat = new HashMap<>();
                    ProcessBuilder pb = new ProcessBuilder("iostat", "-dx", "1", "2");
                    Process p = pb.start();

                    // 读取输出
                    List<String> lines = new ArrayList<>();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // 解析最后一部分数据（跳过首次统计）
                    boolean startParsing = false;
                    for (String line : lines) {
                        if (line.startsWith("Device")) {
                            startParsing = true;
                            continue;
                        }
                        if (startParsing && !line.trim().isEmpty()) {
                            System.out.println("读取硬盘速度"+line.trim());
                            Map<String, String> iostat = new HashMap<>();
                            String[] parts = line.trim().split("\\s+");
                            iostat.put("read", parts[2]);
                            iostat.put("write", parts[9]);
                            iostat.put("util", parts[22]);
                            hardDiskIostat.put(parts[0], iostat);
                        }
                    }
                    GlobalCache.put("hardDiskIostat", hardDiskIostat);

                    // 获取所有网络接口
                    List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
                    List<NetworkIF> networkIF = networkIFs.stream()
                            .filter(net -> net.getName().equals("wireless_32768")).collect(Collectors.toList());
                    NetworkIF eth0 = networkIF.get(0);
                    // 第一次采样（获取初始数据）
                    eth0.updateAttributes();
                    // 计算速率
                    String name = eth0.getName();
                    long bytesRecvBefore = eth0.getBytesRecv();  // 初始下载字节数
                    long bytesSentBefore = eth0.getBytesSent();  // 初始上传字节数
                    // 等待1秒后第二次采样（计算速率）
                    TimeUnit.SECONDS.sleep(1);

                    // 更新数据
                    eth0.updateAttributes();

                    long bytesRecvAfter = eth0.getBytesRecv();
                    long bytesSentAfter = eth0.getBytesSent();

                    // 计算1秒内的速率（字节/秒 → KB/s 或 MB/s）
                    long downloadRate = bytesRecvAfter - bytesRecvBefore;
                    long uploadRate = bytesSentAfter - bytesSentBefore;
                    GlobalCache.put("uploadRate", uploadRate);
                    GlobalCache.put("downloadRate", downloadRate);
                    // 控制执行频率
                    Thread.sleep(1000);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        });
        
        continuousThread.setName("Continuous-Worker-Thread");
        continuousThread.start();
    }

    // 如果需要停止线程，可以添加这个方法
    public void stop() {
        this.running = false;
    }
}