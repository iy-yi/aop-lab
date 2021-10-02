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

	@AfterReturning(
			pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))",
			returning="result")
	public void afterCreateSecret(JoinPoint joinPoint, Object result) {
//		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		UUID secretId = (UUID)result;
		String creator = joinPoint.getArgs()[0].toString();
		String secretContent = joinPoint.getArgs()[1].toString();
		// add secret creator
		SecretStatsImpl.secretOwnerMap.put(secretId, creator);
		// add creator to secretAllowReadMap and secretReadMap
		SecretStatsImpl.secretAllowReadMap.computeIfAbsent(secretId, v -> new HashSet<>()).add(creator);
		SecretStatsImpl.secretReadMap.computeIfAbsent(secretId, v -> new HashSet<>()).add(creator);
		// update length of the longest secret
		int contentLen = secretContent.length();
		if ( contentLen > SecretStatsImpl.lengthOfLongestSecret) {
			SecretStatsImpl.lengthOfLongestSecret = contentLen;
		}
	}

	@AfterReturning(
			pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))",
			returning="result")
	public void afterReadSecret(JoinPoint joinPoint, Object result) {
//		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String userId = (String) args[0];
		UUID secretId = (UUID) args[1];
		String content = (String) result;
		// add user to secretReadMap
		SecretStatsImpl.secretReadMap.get(secretId).add(userId);
		// add secret content to secretContentMap
		SecretStatsImpl.secretContentMap.putIfAbsent(secretId, content);
	}

	@AfterReturning( pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void afterShareSecret(JoinPoint joinPoint) {
		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String targetUserId = (String) args[2];
		UUID secretId = (UUID) args[1];
		String fromUser = (String) args[0];

		// add user to secretAllowReadMap
		SecretStatsImpl.secretAllowReadMap.get(secretId).add(targetUserId);

		// update userInboundShareMap of target user
		String shareInfo = fromUser + "+" + secretId;
		SecretStatsImpl.userInboundShareMap.computeIfAbsent(targetUserId, v-> new HashSet<>()).add(shareInfo);

		// update userOutboundReshareMap for the fromUser
		if (SecretStatsImpl.secretOwnerMap.get(secretId) != fromUser) {
			String reshareInfo = targetUserId + "+" + secretId;
			SecretStatsImpl.userOutReshareMap.computeIfAbsent(fromUser, v -> new HashSet<>()).add(reshareInfo);
		}
	}

	@AfterReturning( pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void afterUnshareSecret(JoinPoint joinPoint) {
//		System.out.printf("Stats After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();
		String targetUserId = (String) args[2];
		UUID secretId = (UUID) args[1];
		// remove user from secretAllowReadMap
		SecretStatsImpl.secretAllowReadMap.get(secretId).remove(targetUserId);
	}
	
//	@Before("execution(public void edu.sjsu.cmpe275.aop.SecretService.*(..))")
//	public void dummyBeforeAdvice(JoinPoint joinPoint) {
//		System.out.printf("Doing stats before the executuion of the metohd %s\n", joinPoint.getSignature().getName());
//	}
	
}
