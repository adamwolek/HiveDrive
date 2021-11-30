package org.hivedrive.cmd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;

@Configuration
//@EnableLoadTimeWeaving(aspectjWeaving=AspectJWeaving.ENABLED)
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class AOPConfig {
 
}