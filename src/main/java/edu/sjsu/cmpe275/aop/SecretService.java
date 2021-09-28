package edu.sjsu.cmpe275.aop;

import java.io.IOException;
import java.util.UUID;

public interface SecretService {
	// Please do NOT change this file, and it is NOT part of your submission.

	UUID createSecret(String userId, String secretContent) throws IOException, IllegalArgumentException;


	String readSecret(String userId, UUID secretId)
	   		 throws IOException, IllegalArgumentException, NotAuthorizedException;

	void shareSecret(String userId, UUID secretId, String targetUserId)
			throws IOException, IllegalArgumentException, NotAuthorizedException;

    void unshareSecret(String userId, UUID secretId, String targetUserId)
      		 throws IOException, IllegalArgumentException, NotAuthorizedException;

}
