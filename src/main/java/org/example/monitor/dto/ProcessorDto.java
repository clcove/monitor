package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * ProcessorDto 是一个值容器，用于显示处理器主体信息
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class ProcessorDto
{
    /**
     * 处理器名称字段
     */
    private String name;

    /**
     * 核心线程数字段
     * 8c/16t
     */
    private String coreCount;

    /**
     * 处理器频率
     */
    private String clockSpeed;

    /**
     * 处理器占用率
     */
    private int usage;

    /**
     * 处理器温度
     */
    private String temp;

}