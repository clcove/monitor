package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * HardDiskDto 是用于显示硬盘信息的值容器
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class HardDiskDto
{
    /**
     *  名称
     */
    private String name;

    /**
     * 硬盘型号
     */
    private String model;

    /**
     * 硬盘序列号
     */
    private String serial;

    /**
     * 硬盘大小
     */
    private String total;

    /**
     * 硬盘占用率
     */
    private int usage;

    /**
     * 硬盘读取
     */
    private String read;

    /**
     * 硬盘写入
     */
    private String write;

    /**
     * 硬盘所属存储空间
     */
    private String theStorageSpaceYouBelongTo;

    /**
     * 硬盘温度
     */
    private String temp;
}