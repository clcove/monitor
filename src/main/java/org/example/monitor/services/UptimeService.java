package org.example.monitor.services;


import org.example.monitor.dto.UptimeDto;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;

@Service
public class UptimeService
{

    private final SystemInfo systemInfo = new SystemInfo();

    /**
     * Gets uptime information
     *
     * @return UptimeDto with filled fields
     */
    public UptimeDto getUptime()
    {
        UptimeDto uptimeDto = new UptimeDto();

        long uptimeInSeconds = systemInfo.getOperatingSystem().getSystemUptime();

        uptimeDto.setDays(String.format("%02d", (int) uptimeInSeconds / 86400));
        uptimeDto.setHours(String.format("%02d", (int) (uptimeInSeconds % 86400) / 3600));
        uptimeDto.setMinutes(String.format("%02d", (int) (uptimeInSeconds / 60) % 60));
        uptimeDto.setSeconds(String.format("%02d", (int) uptimeInSeconds % 60));

        return uptimeDto;
    }
}