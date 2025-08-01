package org.example.monitor.services;


import org.example.monitor.dto.*;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.Util;

import javax.xml.crypto.Data;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * InfoService provides various information about machine, such as processor name, core count, Ram amount, etc.
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@Service
public class InfoService
{

    private final SystemInfo systemInfo = new SystemInfo();
    /**
     * 用于将 dto 传送到相应的控制器
     *
     * @return InfoDto filled with server info
     */
    public InfoDto getInfo()
    {
        InfoDto infoDto = new InfoDto();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        //cpu信息
        long l = System.currentTimeMillis();
        System.out.println("cpu查询前:"+l);
        infoDto.setProcessor(getProcessor(hardware));
        //内存信息
        System.out.println("内存查询前:"+System.currentTimeMillis());
        infoDto.setMachine(getMachine(hardware));
        //gpu信息
        System.out.println("gpu查询前:"+System.currentTimeMillis());
        infoDto.setGraphics(getGraphics(hardware));
        //存储信息
        System.out.println("存储查询前:"+System.currentTimeMillis());
        infoDto.setStorage(getStorage(hardware));
        //硬盘信息
        System.out.println("硬盘查询前:"+System.currentTimeMillis());
        infoDto.setHardDisks(getHardDisk(hardware));
        //网络信息
        System.out.println("网卡查询前:"+System.currentTimeMillis());
        infoDto.setNetworks(getNetwork(hardware));
        System.out.println("全部查询结束:"+System.currentTimeMillis());
        return infoDto;
    }


    /**
     * 读取cpu信息
     *
     * @return ProcessorDto with filled fields
     */
    private ProcessorDto getProcessor(HardwareAbstractionLayer hardware) {
        ProcessorDto processorDto = new ProcessorDto();
        //cpu 信息
        CentralProcessor centralProcessor = hardware.getProcessor();

        // cpu型号
        String name = centralProcessor.getProcessorIdentifier().getName().split("@")[0].trim();
        processorDto.setName(name);

        // 核心数
        int coreCount = centralProcessor.getPhysicalProcessorCount();

        //线程数
        int threads = centralProcessor.getLogicalProcessorCount();
        processorDto.setCoreCount(coreCount+"c/"+threads+"t");

        // cpu频率
        processorDto.setClockSpeed(getConvertedFrequency(centralProcessor.getCurrentFreq()));

        // cpu使用率
        processorDto.setUsage(getProcessorUsage(hardware));

        //cpu温度
        processorDto.setTemp(getProcessorTemp() + "°C");
        return processorDto;
    }

    /**
     * 读取CPU温度
     *
     * @return
     */
    private String getProcessorTemp(){
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        String processorTemp = null;
        if (os.getFamily().contains("Linux")) {
            List<String> gemObjects = ExecutingCommand.runNative("sudo cat /sys/class/thermal/thermal_zone3/temp");
            System.out.println("GPU频率查询输出"+gemObjects.size());
            for (String line : gemObjects) {
                System.out.println("GPU频率查询输出"+line);
                double temp = Integer.parseInt(line)/1000.0;
                processorTemp = String.valueOf(temp);
            }

        } else if (os.getFamily().contains("Windows")) {
            // Windows系统实现
            System.out.println("读取CPU温度:不支持的操作系统"+os.getFamily());
        } else {
            System.out.println("读取CPU温度:不支持的操作系统"+os.getFamily());
        }
        return processorTemp;
    }

    /**
     * 读取内存信息
     *
     * @return MachineDto with filled fields
     */
    private MachineDto getMachine(HardwareAbstractionLayer hardware) {
        MachineDto machineDto = new MachineDto();

        //内存信息
        GlobalMemory globalMemory = hardware.getMemory();

        //内存总大小
        long totalRam = globalMemory.getTotal();
        machineDto.setTotalRam(getConvertedCapacity(totalRam) + " RAM");

        //内存类型 ddr4
        Optional<PhysicalMemory> physicalMemoryOptional = globalMemory.getPhysicalMemory().stream().findFirst();
        String ramTypeOrOSBitDepth = physicalMemoryOptional.get().getMemoryType();
        machineDto.setRamTypeOrOSBitDepth(ramTypeOrOSBitDepth);

        //内存使用率
        machineDto.setUsage(getRamUsage(hardware));

        //内存频率
        machineDto.setClockSpeed(getRamFrequency());

        //swap信息
        machineDto.setSwapAmount(getConvertedCapacity(globalMemory.getVirtualMemory().getSwapTotal()) + " Swap");

        return machineDto;
    }

    /**
     * 读取gpu信息
     *
     * @return GraphicsDto with filled fields
     */
    private GraphicsDto getGraphics(HardwareAbstractionLayer hardware) {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        GraphicsDto graphicsDto = new GraphicsDto();
        //显卡信息
        List<GraphicsCard> gpus = hardware.getGraphicsCards();
        GraphicsCard gpu = gpus.get(0);
        //gpu型号
        graphicsDto.setName(extractFirstOrSelf(gpu.getName()));
        //显存大小
        graphicsDto.setMemory(getConvertedCapacity(gpu.getVRam()));

        //显存占用
        graphicsDto.setMemoryUsage(getConvertedCapacity(getGraphicsMemoryUsage()));

        //gpu占用
        graphicsDto.setUsage(getGraphicsUsage());

        //gpu频率
        graphicsDto.setClockSpeed(getGpuDetailsFrequency());
        return graphicsDto;
    }

    /**
     * 提取[]中的内容
     *
     * @return GraphicsDto with filled fields
     */
    public  String extractFirstOrSelf(String input) {
        int start = input.indexOf('[');
        int end = input.indexOf(']');

        if (start != -1 && end != -1 && start < end) {
            return input.substring(start + 1, end);
        }
        return input;
    }
    /**
     * 读取存储信息
     *
     * @return GraphicsDto with filled fields
     */
    private StorageDto getStorage(HardwareAbstractionLayer hardware)
    {
        StorageDto storageDto = new StorageDto();
        List<HWDiskStore> hwDiskStores = hardware.getDiskStores();

        // 硬盘名称
        String mainStorage = hwDiskStores.isEmpty() ? "Undefined"
            : hwDiskStores.get(0).getModel().replaceAll("\\(.+?\\)", "").trim();
        storageDto.setMainStorage(mainStorage);

        //存储总大小
        long total = hwDiskStores.stream().mapToLong(HWDiskStore::getSize).sum();
        storageDto.setTotal(getConvertedCapacity(total) + " Total");

        //硬盘总数
        int diskCount = hwDiskStores.size();
        storageDto.setDiskCount(diskCount + (diskCount > 1 ? " Disks" : " Disk"));

        //存储空间占用
        storageDto.setUsage(getStorageUsage(hardware));
        return storageDto;
    }
    /**
     * 读取硬盘信息
     *
     * @return GraphicsDto with filled fields
     */
    private List<HardDiskDto> getHardDisk(HardwareAbstractionLayer hardware)
    {
        List<HardDiskDto> hardDiskDtos = new ArrayList<>();
        List<HWDiskStore> hwDiskStores = hardware.getDiskStores();

        hwDiskStores.forEach(hwDiskStore -> {
            HardDiskDto hardDiskDto = new HardDiskDto();
            //硬盘名称
            hardDiskDto.setName(hwDiskStore.getName());
            //硬盘型号
            hardDiskDto.setModel(hwDiskStore.getModel());
            //硬盘序列号
            hardDiskDto.setSerial(hwDiskStore.getSerial());
            //硬盘总大小
            hardDiskDto.setTotal(getConvertedCapacity(hwDiskStore.getSize()));
            //硬盘读取
            hardDiskDto.setRead(getConvertedSize(hwDiskStore.getReads()));
            //硬盘写入
            hardDiskDto.setWrite(getConvertedSize(hwDiskStore.getWrites()));
            //硬盘温度
            hardDiskDto.setTemp("35");
            hardDiskDtos.add(hardDiskDto);
        });
        return hardDiskDtos;
    }
    /**
     * 读取网络信息
     *
     * @return GraphicsDto with filled fields
     */
    private List<NetworkDto> getNetwork(HardwareAbstractionLayer hardware)
    {
        List<NetworkDto> networkDtos = new ArrayList<>();
        List<NetworkIF> networkIFs = hardware.getNetworkIFs();

        networkIFs.forEach(networkIF -> {
            NetworkDto networkDto = new NetworkDto();
            networkDto.setName(networkIF.getName());
            networkDto.setDisplayName(networkIF.getDisplayName());
            networkDto.setMacaddr(networkIF.getMacaddr());
            networkDto.setIPv4addr(networkIF.getIPv4addr());
            networkDto.setIPv6addr(networkIF.getIPv6addr());
            networkDto.setDownload(getConvertedSize(networkIF.getBytesRecv()));
            networkDto.setUpload(getConvertedSize(networkIF.getBytesSent()));
            networkDtos.add(networkDto);
        });
        return networkDtos;
    }


    /**
     * cpu占用
     *
     * @return int that display processor usage
     */
    private int getProcessorUsage(HardwareAbstractionLayer hardware) {
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

        // Handle possible division by zero
        if (totalTicksDelta == 0) {
            return 0; // or handle in a way suitable for your application
        }

        // Calculate CPU usage percentage
        return (int) ((1 - (double) idleTicksDelta / totalTicksDelta) * 100);
    }

    /**
     * 内存占用
     *
     * @return int that display ram usage
     */
    private int getRamUsage(HardwareAbstractionLayer hardware) {
        GlobalMemory globalMemory = hardware.getMemory();
        long totalMemory = globalMemory.getTotal();
        long availableMemory = globalMemory.getAvailable();

        // Handle possible division by zero
        if (totalMemory == 0) {
            return 0; // or handle in a way suitable for your application
        }

        // Calculate RAM usage percentage
        return (int) (100 - ((double) availableMemory / totalMemory * 100));
    }

    /**
     * 存储空间总空间占用
     *
     * @return int that display storage usage
     */
    private int getStorageUsage(HardwareAbstractionLayer hardware) {
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

        // Calculate total storage and free storage for all drives
        long totalStorage = 0;
        long freeStorage = 0;
        for (OSFileStore fileStore : fileSystem.getFileStores()) {
            totalStorage += fileStore.getTotalSpace();
            freeStorage += fileStore.getFreeSpace();
        }

        // Handle possible division by zero
        if (totalStorage == 0) {
            return 0; // or handle in a way suitable for your application
        }

        // Calculate total storage usage percentage for all drives
        return (int) Math.round(((double) (totalStorage - freeStorage) / totalStorage) * 100);
    }

    /**
     * 读取内存频率
     *
     * @return 3200 MHz
     */
    private String getRamFrequency(){
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        String ramFrequency = null;
        if (os.getFamily().contains("Linux")) {
            // 方法1: 使用dmidecode
            List<String> dmidecodeOutput = ExecutingCommand.runNative("sudo dmidecode --type memory");
            for (String line : dmidecodeOutput) {
                if (line.contains("Speed:") && !line.contains("Unknown")) {
                    ramFrequency = line.trim().replaceAll("[^0-9]", "") + " MHz";
                }
            }

        } else if (os.getFamily().contains("Windows")) {
            // Windows系统实现
            List<String> wmicOutput = ExecutingCommand.runNative("wmic memorychip get speed");
            for (String line : wmicOutput) {
                if (!line.trim().equals("Speed") && !line.trim().isEmpty()) {
                    ramFrequency = line.trim() + " MHz";
                }
            }
        } else {
            System.out.println("内存频率读取:不支持的操作系统"+os.getFamily());
        }
        return ramFrequency;
    }

    /**
     * 读取GPU频率
     *
     * @return 320 MHz
     */
    private String getGpuDetailsFrequency(){
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        String ramFrequency = null;
        if (os.getFamily().contains("Linux")) {
            // 方法1:
            List<String> gemObjects = ExecutingCommand.runNative("cat /sys/kernel/debug/dri/0/i915_frequency_info | grep \"Actual freq\"");
            for (String line : gemObjects) {
//                System.out.println("GPU频率查询输出"+line);
                if (line.contains("Actual")) {
                    ramFrequency = line.trim().replaceAll("[^0-9]", "") + " MHz";
                }
            }

        } else if (os.getFamily().contains("Windows")) {
            // Windows系统实现
            System.out.println("读取GPU频率:不支持的操作系统"+os.getFamily());
        } else {
            System.out.println("读取GPU频率:不支持的操作系统"+os.getFamily());
        }
        return ramFrequency;
    }

    /**
     * 读取GPU显存占用
     *
     * @return 350 MHz
     */
    private long getGraphicsMemoryUsage(){
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        long ramFrequency = 0;
        if (os.getFamily().contains("Linux")) {
            // 方法1:
            List<String> gemObjects = ExecutingCommand.runNative("cat /sys/kernel/debug/dri/0/i915_gem_objects");
            for (String line : gemObjects) {
//                System.out.println("GPU频率查询输出"+line);
                if (line.contains("shrinkable")) {
                    String[] parts = line.split(",");
                    if (parts.length > 1) {
                        // 提取 "212799488 bytes" 中的数字
                        String bytesPart = parts[1].trim(); // 去除前后空格
                        String bytesStr = bytesPart.split("\\s+")[0]; // 按空格分割取第一个词
                        ramFrequency = Long.parseLong(bytesStr);
                    }
                }
            }

        } else if (os.getFamily().contains("Windows")) {
            // Windows系统实现
            System.out.println("读取GPU显存占用:不支持的操作系统"+os.getFamily());
        } else {
            System.out.println("读取GPU显存占用:不支持的操作系统"+os.getFamily());
        }
        return ramFrequency;
    }

    /**
     * 读取GPU占用
     *
     * @return 350 MHz
     */
    private int getGraphicsUsage(){
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        int ramFrequency = 10;
        if (os.getFamily().contains("Linux")) {
            // 方法1:
            List<String> gemObjects = ExecutingCommand.runNative("sudo timeout 0.05 intel_gpu_top -l -s 1");
            for (String line : gemObjects) {
//                System.out.println("GPU占用查询输出"+line);
                if (!line.contains("Freq")&& !line.contains("req")) {
                    // 1. 提取所有两位小数
                    List<Double> decimals = new ArrayList<>();
                    Matcher matcher = Pattern.compile("\\d+\\.\\d{2}").matcher(line);
                    while (matcher.find()) {
                        decimals.add(Double.parseDouble(matcher.group()));
                    }

                    // 2. 取最后4个并四舍五入
                    List<Integer> rounded = decimals.stream()
                            .skip(Math.max(0, decimals.size() - 4))
                            .map(d -> (int) Math.round(d))
                            .collect(Collectors.toList());

                    // 3. 找出最大值
                    ramFrequency = rounded.stream().max(Integer::compare).orElse(0);
                }
            }

        } else if (os.getFamily().contains("Windows")) {
            // Windows系统实现
            System.out.println("读取GPU占用:不支持的操作系统"+os.getFamily());
        } else {
            System.out.println("读取GPU占用:不支持的操作系统"+os.getFamily());
        }
        return ramFrequency;
    }

    /**
     * 将频率转换为最易读的格式
     *
     * @param hertzArray raw frequency array values in hertz for each logical processor
     * @return String with formatted frequency and postfix
     */
    private String getConvertedFrequency(final long[] hertzArray)
    {
        long totalFrequency = Arrays.stream(hertzArray).sum();
        long hertz = totalFrequency / hertzArray.length;

        if ((hertz / 1E+6) > 999)
        {
            return (Math.round((hertz / 1E+9) * 10.0) / 10.0) + " GHz";
        }
        else
        {
            return Math.round(hertz / 1E+6) + " MHz";
        }
    }

    /**
     * 将容量转换为最易读的格式
     *
     * @param bits raw capacity value in bits
     * @return String with formatted capacity and postfix
     */
    private String getConvertedCapacity(final long bits)
    {
        DecimalFormat df = new DecimalFormat("#.##"); // 最多保留两位小数，并去掉末尾的0
        df.setDecimalSeparatorAlwaysShown(false); // 不显示多余的小数点

        if ((bits / 1.049E+6) > 999) {
            if ((bits / 1.074E+9) > 999) {
                double value = Math.ceil((bits / 1.1E+12) * 10.0) / 10.0;
                return df.format(value) + " TB";
            } else {
                long value = (long) Math.ceil(bits / 1.074E+9);
                return df.format(value) + " GB";
            }
        } else {
            long value = (long) Math.ceil(bits / 1.049E+6);
            return df.format(value) + " MB";
        }
    }

    /**
     * 将字节数转换为最易读的格式
     *
     * @param bytes raw byte value
     * @return String with formatted size and postfix (e.g., "1.5 KB", "2.3 GB")
     */
    private String getConvertedSize(final long bytes) {
        DecimalFormat df = new DecimalFormat("#.##"); // 最多保留两位小数，并去掉末尾的0
        df.setDecimalSeparatorAlwaysShown(false); // 不显示多余的小数点

        // 定义字节单位的阈值 (基于 1024)
        final double KB = 1024.0;
        final double MB = KB * 1024.0; // 1,048,576
        final double GB = MB * 1024.0; // 1,073,741,824
        final double TB = GB * 1024.0; // 1,099,511,627,776
        final double PB = TB * 1024.0; // 1,125,899,906,842,624

        // 根据字节数大小选择合适的单位
        if (bytes < KB) {
            // 小于 1KB，直接显示字节
            return bytes + " B";
        } else if (bytes < MB) {
            // KB 级别
            double value = Math.ceil((bytes / KB) * 10.0) / 10.0; // 保留一位小数向上取整
            return df.format(value) + " KB";
        } else if (bytes < GB) {
            // MB 级别
            double value = Math.ceil((bytes / MB) * 10.0) / 10.0; // 保留一位小数向上取整
            return df.format(value) + " MB";
        } else if (bytes < TB) {
            // GB 级别
            double value = Math.ceil((bytes / GB) * 10.0) / 10.0; // 保留一位小数向上取整
            return df.format(value) + " GB";
        } else if (bytes < PB) {
            // TB 级别
            double value = Math.ceil((bytes / TB) * 10.0) / 10.0; // 保留一位小数向上取整
            return df.format(value) + " TB";
        } else {
            // PB 级别及以上
            double value = Math.ceil((bytes / PB) * 10.0) / 10.0; // 保留一位小数向上取整
            return df.format(value) + " PB";
        }
    }
}