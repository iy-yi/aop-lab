package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.NotAuthorizedException;
import edu.sjsu.cmpe275.aop.SecretStatsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.Set;
import java.util.UUID;

@Aspect
@Order(2)
public class AccessControlAspect {

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))")
	public void readAccessCheck(JoinPoint joinPoint) {
//		System.out.printf("Access control prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());

		Object[] args = joinPoint.getArgs();
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];

		Set<String> allowReadSet = SecretStatsImpl.secretAllowReadMap.containsKey(secretId) ?
				SecretStatsImpl.secretAllowReadMap.get(secretId) : null;
		if (allowReadSet == null || !allowReadSet.contains(userId)) {
			throw new NotAuthorizedException("Not Authorized to read");
		}
	}

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void shareAccessCheck(JoinPoint joinPoint) {
//		System.out.printf("Access control prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());

		Object[] args = joinPoint.getArgs();
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];

		// not authorized to share if the user cannot read at this time
		Set<String> allowReadSet = SecretStatsImpl.secretAllowReadMap.getOrDefault(secretId, null);
		if (allowReadSet == null || !allowReadSet.contains(userId)) {
			throw new NotAuthorizedException("Not Authorized to share");
		}

		// not authorized to share if the user has not read
		Set<String> readSet = SecretStatsImpl.secretReadMap.getOrDefault(secretId, null);
		if (readSet == null || !readSet.contains(userId)) {
			throw new NotAuthorizedException("Not Authorized to share");
		}
	}

	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void unshareAccessCheck(JoinPoint joinPoint) {
//		System.out.printf("Access control prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());

		Object[] args = joinPoint.getArgs();
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];
		String targetUserId = (String) args[2];

		if (!SecretStatsImpl.secretOwnerMap.containsKey(secretId) ||
				SecretStatsImpl.secretOwnerMap.get(secretId) != userId) {
			throw new NotAuthorizedException("Not Authorized to share");
		}
		if (!SecretStatsImpl.secretAllowReadMap.get(secretId).contains(targetUserId)) {
			throw new NotAuthorizedException("User is not shared");
		}
	}
}
