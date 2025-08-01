package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * StorageDto 是用于显示存储主体信息的值容器
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class StorageDto
{
    /**
     * Host0 存储名称字段
     */
    private String mainStorage;

    /**
     * 存储总量
     */
    private String total;

    /**
     * 存储总占用率
     */
    private int usage;

    /**
     * 磁盘总数字段
     */
    private String diskCount;

    /**
     * 总读写
     */
    private String readAndWrite;
}