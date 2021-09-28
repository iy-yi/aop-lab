package edu.sjsu.cmpe275.aop;

import java.util.UUID;

public interface SecretStats {
	// Please do NOT change this file.

	void resetStatsAndSystem();

	int getLengthOfLongestSecret();

	String getMostTrustedUser();

	String getWorstSecretKeeper();

	UUID getBestKnownSecret();

}