package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.SecretStatsImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Aspect
@Order(0)
public class StatsAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */

	@Autowired SecretStatsImpl stats;

	//TODO:stats.resetStats();

	@AfterReturning(
			pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))",
			returning="result")
	public void afterCreateSecret(JoinPoint joinPoint, Object result) {
		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		UUID secretId = (UUID)result;
		String creator = joinPoint.getArgs()[0].toString();
		// add secret creator
		SecretStatsImpl.secretOwnerMap.put(secretId, creator);
		// add creator to secretAllowReadMap and secretReadMap
		Set<String> allowReadSet = new HashSet<>();
		allowReadSet.add(creator);
		SecretStatsImpl.secretAllowReadMap.put(secretId, allowReadSet);
		SecretStatsImpl.secretReadMap.put(secretId, allowReadSet);
	}

	@AfterReturning(
			pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))",
			returning="result")
	public void afterReadSecret(JoinPoint joinPoint, Object result) {
		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];
		// add user to secretReadMap
		Set<String> readerSet = SecretStatsImpl.secretAllowReadMap.get(secretId);
		readerSet.add(userId);
		SecretStatsImpl.secretReadMap.put(secretId,readerSet);
	}

	@After("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void afterShareSecret(JoinPoint joinPoint) {
		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String targetUserId = (String) args[2];
		UUID secretId = (UUID) args[1];
		// add user to secretReadMap
		Set<String> allowReadSet = SecretStatsImpl.secretAllowReadMap.get(secretId);
		allowReadSet.add(targetUserId);
		SecretStatsImpl.secretReadMap.put(secretId, allowReadSet);
	}

	@After("execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void afterUnshareSecret(JoinPoint joinPoint) {
		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String targetUserId = (String) args[2];
		UUID secretId = (UUID) args[1];
		// add user to secretReadMap
		Set<String> allowReadSet = SecretStatsImpl.secretAllowReadMap.get(secretId);
		allowReadSet.remove(targetUserId);
		SecretStatsImpl.secretReadMap.put(secretId, allowReadSet);
	}
	
	@Before("execution(public void edu.sjsu.cmpe275.aop.SecretService.*(..))")
	public void dummyBeforeAdvice(JoinPoint joinPoint) {
		System.out.printf("Doing stats before the executuion of the metohd %s\n", joinPoint.getSignature().getName());
	}
	
}
