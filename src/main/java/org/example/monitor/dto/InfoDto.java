package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * InfoDto 是其他信息对象的容器
 *
 * @author Rudolf Barbu
 * @version 1.0.1
 */
@Getter
@Setter
public class InfoDto
{
    /**
     * 处理器信息 dto 字段
     */
    private ProcessorDto processor;

    /**
     *  内存信息 dto 字段
     */
    private MachineDto machine;

    /**
     * gpu信息 dto 字段
     */
    private GraphicsDto graphics;

    /**
     *  存储信息 dto 字段
     */
    private StorageDto storage;

    /**
     *  存储信息 dto 字段
     */
    private List<HardDiskDto> hardDisks;

    /**
     *  网络信息 dto 字段
     */
    private NetworkDto network;
}