package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.SecretStatsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.UUID;

@Aspect
@Order(1)
public class ValidationAspect {

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))")
	public void validCreateSecret(JoinPoint joinPoint) {
//		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		if (args[0] == null || args[0].toString().length() == 0) {
			throw new IllegalArgumentException("Invalid userID");
		}
		if (args[1] == null || (args[1].toString().length() > 128) || args[1].toString().length() == 0) {
			throw new IllegalArgumentException("Invalid secretContent");
		}
	}

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))")
	public void validReadSecret(JoinPoint joinPoint) {
//		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		for (Object arg: args) {
			if (arg == null || arg.toString().length() == 0) {
				throw new IllegalArgumentException("NULL or empty argument");
			}
		}
	}

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void validShareSecret(JoinPoint joinPoint) {
//		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		for (Object arg: args) {
			if (arg == null || arg.toString().length() == 0) {
				throw new IllegalArgumentException("NULL argument");
			}
		}
		String userId = (String) args[0];
		String targetUser = (String) args[2];
		if (userId.compareTo(targetUser) == 0) {
			throw new IllegalArgumentException("Equal UserId and TargetUserId");
		}
	}

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void validUnshareSecret(JoinPoint joinPoint) {
//		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		for (Object arg: args) {
			if (arg == null || arg.toString().length() == 0) {
				throw new IllegalArgumentException("NULL argument");
			}
		}
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];
		String targetUserId = (String) args[2];
		if (userId.compareTo(targetUserId) == 0 && SecretStatsImpl.secretOwnerMap.containsKey(secretId)
				&& SecretStatsImpl.secretOwnerMap.get(secretId) == userId) {
			throw new IllegalArgumentException("Un-share from himself");
		}
	}
}
