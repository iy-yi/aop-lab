package edu.sjsu.cmpe275.aop;

import java.util.*;

public class SecretStatsImpl implements SecretStats {
    /***
     * This is a dummy implementation only.
     * You are expected to provide an actual implementation based on the requirements.
     */
	public static Map<UUID, String> secretOwnerMap = new HashMap<>();
	// un-shared user is removed
	public static Map<UUID, Set<String>> secretAllowReadMap = new HashMap<>();
	public static Map<UUID, Set<String>> secretReadMap = new HashMap<>();

	// return getBestKnownSecret(); not count un-sharing?
	public static Map<UUID, Set<String>> secretSharedMap = new HashMap<UUID, Set<String>>();
	// Map<user, UUID+sharedFrom>
	public static Map<String, Set<String>> userInboundSecretMap = new HashMap<>();
	// Map<user, UUID+sharedTo>
	public static Map<String, Set<String>> userOutboundSecretMap = new HashMap<>();

	@Override
	public void resetStatsAndSystem() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLengthOfLongestSecret() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMostTrustedUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorstSecretKeeper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getBestKnownSecret() {
		// TODO Auto-generated method stub
		return null;
	}
    
}



