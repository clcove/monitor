package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * MachineDto 是一个值容器，用于显示机器主体信息
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class MachineDto
{

    /**
     * 总安装的 内存大小
     */
    private String totalRam;

    /**
     * 内存频率
     */
    private String clockSpeed;

    /**
     * 内存代数字段
     */
    private String ramTypeOrOSBitDepth;

    /**
     * 内存占用率
     */
    private int usage;

    /**
     * 虚拟内存总量（Linux 上的交换）字段
     */
    private String swapAmount;
}