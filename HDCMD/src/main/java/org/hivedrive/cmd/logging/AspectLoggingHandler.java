package org.hivedrive.cmd.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hivedrive.cmd.service.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectLoggingHandler {

	Logger logger = LoggerFactory.getLogger(ConnectionService.class);
	
	@Around("@annotation(javax.annotation.PostConstruct)")
	public void logAround(ProceedingJoinPoint joinPoint) throws Throwable {
	    System.out.println("method name: " + joinPoint.getSignature().getName());
	}
	
	
	@Before("execution(* org.hivedrive.cmd.service.ConnectionService.*(..))")
    public void logConnectionService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.UserKeysService.*(..))")
    public void logKeysService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.AsymetricEncryptionService.*(..))")
    public void logAsymetricEncryptionService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.FileCompresssingService.*(..))")
    public void logFileCompresssingService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.FileSplittingService.*(..))")
    public void logFileSplittingService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.RepositoryConfigService.*(..))")
    public void logRepositoryConfigService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.SignatureService.*(..))")
    public void logSignatureService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
	@Before("execution(* org.hivedrive.cmd.service.SymetricEncryptionService.*(..))")
    public void logSymetricEncryptionService(JoinPoint joinPoint) { 
		System.out.println(joinPoint);
	}
	
}
