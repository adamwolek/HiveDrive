package org.hivedrive.cmd.config;

import org.hivedrive.cmd.service.UserKeysService;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class HDTestSpringExtension extends SpringExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;

    
    @Override
    public void beforeAll(ExtensionContext context) {
    	ApplicationContext applicationContext = super.getApplicationContext(context);
    	UserKeysService userKeysService = applicationContext.getBean(UserKeysService.class);
        if (!started) {
            started = true;
            userKeysService.setKeys(userKeysService.generateNewKeys());
        }
    }

    @Override
    public void close() {
        
    }
}
