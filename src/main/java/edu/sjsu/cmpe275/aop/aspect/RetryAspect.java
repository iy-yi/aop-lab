package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.aspectj.lang.annotation.Around;

import java.io.IOException;

@Aspect
@Order(1)
public class RetryAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */
	private static int MAX_TRIES = 3;

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretService.*(..))")
	public Object retryAround(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.printf("Retry aspect prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		IOException exception = null;

		for (int i = 1; i <= MAX_TRIES; i++) {
			try {
				return joinPoint.proceed();
			} catch (IOException e) {
				System.out.println("IOException during try #" + i);
				exception = e;
			}
		}
		throw exception;
	}

}
