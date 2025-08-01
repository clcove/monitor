package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * GraphicsDto 是一个值容器，用于显示gpu信息
 *
 * @author mzy
 * @version 1.0.0
 */
@Getter
@Setter
public class GraphicsDto
{
    /**
     * gpu名称字段
     */
    private String name;

    /**
     * 显存大小字段
     *
     */
    private String memory;

    /**
     * 显存占用
     */
    private String memoryUsage;

    /**
     * gpu占用率
     */
    private int usage;

    /**
     * gpu频率
     */
    private String clockSpeed;
}