package edu.sjsu.cmpe275.aop;

import java.util.*;

public class SecretStatsImpl implements SecretStats {

	public static Map<UUID, String> secretOwnerMap = new HashMap<>();
	// un-shared user is removed
	public static Map<UUID, Set<String>> secretAllowReadMap = new HashMap<>();
	// un-shared user is not removed. include secret owner.
	public static Map<UUID, Set<String>> secretReadMap = new HashMap<>();
	public static Map<UUID, String> secretContentMap = new HashMap<>();

	// for stats
	public static int lengthOfLongestSecret = 0;
	// Map<user, sharedFrom+UUID>
	public static Map<String, Set<String>> userInboundShareMap = new HashMap<>();
	// Map<user, sharedTo+UUID>
	public static Map<String, Set<String>> userOutReshareMap = new HashMap<>();


	@Override
	public void resetStatsAndSystem() {
		if (!secretOwnerMap.isEmpty()) {
			secretOwnerMap.clear();
		}
		if (!secretAllowReadMap.isEmpty()) {
			secretAllowReadMap.clear();
		}
		if (!secretReadMap.isEmpty()) {
			secretReadMap.clear();
		}
		if (!secretContentMap.isEmpty()) {
			secretContentMap.clear();
		}
		lengthOfLongestSecret = 0;
		if (!userInboundShareMap.isEmpty()) {
			userInboundShareMap.clear();
		}
		if (!userOutReshareMap.isEmpty()) {
			userOutReshareMap.clear();
		}
	}

	@Override
	public int getLengthOfLongestSecret() {
		return lengthOfLongestSecret;
	}

	@Override
	public String getMostTrustedUser() {
		String mostTrustedUser = null;
		long mostInboundShareCount = 0;
		for (String user: userInboundShareMap.keySet()) {
			Set<String> inboundSet = userInboundShareMap.get(user);
			if ((inboundSet.size() > mostInboundShareCount) || (inboundSet.size() == mostInboundShareCount
					&& user.compareTo(mostTrustedUser) < 0)) {
				mostTrustedUser = user;
				mostInboundShareCount = inboundSet.size();
			}
		}
		System.out.println("Inbound share count: " + mostInboundShareCount);
		return mostTrustedUser;
	}

	@Override
	public String getWorstSecretKeeper() {
		String worstSecretKeeper = null;
		double highestLeakScore = -1;
		for (String user: userInboundShareMap.keySet()) {
			long inboundCount = userInboundShareMap.get(user).size();
			long outboundCount = userOutReshareMap.containsKey(user) ? userOutReshareMap.get(user).size() : 0;
			double leakScore = outboundCount * 1.0 / inboundCount;
			if (leakScore > highestLeakScore || (leakScore == highestLeakScore
					&& user.compareTo(worstSecretKeeper) < 0)) {
				worstSecretKeeper = user;
				highestLeakScore = leakScore;
			}
		}

		System.out.println("Highest leak score: " + highestLeakScore);
		return worstSecretKeeper;
	}

	@Override
	public UUID getBestKnownSecret() {
		UUID bestKnownSecret = null;
		long highestReadCount = 0;
		for (UUID secret: secretReadMap.keySet()) {
			long readCount = secretReadMap.get(secret).size();
			if (readCount > 1 && readCount > highestReadCount) {
				highestReadCount = readCount;
				bestKnownSecret = secret;
			} else if (readCount > 1 && readCount == highestReadCount &&
					secretContentMap.get(secret).compareTo(secretContentMap.get(bestKnownSecret)) < 0) {
				bestKnownSecret = secret;
			}
		}
		highestReadCount--;	// minus the creator
		System.out.println("Highest read count: " + highestReadCount);
		return bestKnownSecret;
	}
    
}



