package com.dmu.debug_visual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dmu.debug_visual", "util"})
public class DebugVisualApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebugVisualApplication.class, args);
    }

}
