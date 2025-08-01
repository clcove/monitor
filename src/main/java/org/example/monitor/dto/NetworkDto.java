package org.example.monitor.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * NetworkDto 是用于显示硬盘信息的值容器
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@Setter
public class NetworkDto
{
    /**
     *  网卡名称
     */
    private String name;

    /**
     * 网卡型号
     */
    private String model;

    /**
     * 网卡显示名称
     */
    private String displayName;


    /**
     * 网卡上传速度
     */
    private String upload;

    /**
     * 网卡下载速度
     */
    private String download;
    /**
     * ipv4地址
     */
    private  String[] iPv4addr;

    /**
     * mac地址
     */
    private String macaddr;

    /**
     * ipv6地址
     */
    private  String[] iPv6addr;
}