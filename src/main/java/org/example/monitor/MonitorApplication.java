package org.example.monitor;

import org.example.monitor.services.ContinuousThreadRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MonitorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MonitorApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  监控面板 moni 启动   ლ(´ڡ`ლ)ﾞ  \n");
        // 通过Spring容器获取bean实例
        ContinuousThreadRunner runner = context.getBean(ContinuousThreadRunner.class);
        runner.run();
    }

}
