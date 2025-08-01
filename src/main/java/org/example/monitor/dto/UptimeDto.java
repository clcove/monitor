package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * UptimeDto 是一个值容器，用于显示正常运行时间主体信息
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class UptimeDto
{
    /**
     * 已运行天数
     */
    private String days;

    /**
     * 已运行小时数
     */
    private String hours;

    /**
     * 已运行分钟数
     */
    private String minutes;

    /**
     * 已运行秒数
     */
    private String seconds;
}