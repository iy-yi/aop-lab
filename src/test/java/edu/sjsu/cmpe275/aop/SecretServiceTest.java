package edu.sjsu.cmpe275.aop;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;


public class SecretServiceTest {

    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
    SecretService secretService = (SecretService) ctx.getBean("secretService");
    SecretStats stats = (SecretStats) ctx.getBean("secretStats");

    @Before
    public void resetBeforeEachTest() {
        stats.resetStatsAndSystem();
    }

    // Create Secret

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_create_emptySecret() throws IOException {
        UUID id = secretService.createSecret("Alice", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_create_exceedLimit() throws IOException {
        UUID id = secretService.createSecret("Alice", "The House on Wednesday passed a bill to suspend the U.S. debt ceiling as the country barrels toward a first-ever default with no.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_create_nullSecret() throws IOException {
        UUID id = secretService.createSecret("Alice", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_create_nullUser() throws IOException {
        UUID id = secretService.createSecret(null, "This is a test secret.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_create_emptyUser() throws IOException {
        UUID id = secretService.createSecret("", "This is a test secret.");
    }

    @Test
    public void test_create() throws IOException {
        UUID id = secretService.createSecret("Alice", "This is a test secret.");
        assertNotNull(id);
    }

    @Test
    public void test_create_duplicate() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "This is a test secret.");
        UUID secret2 = secretService.createSecret("Alice", "This is a test secret.");
        assertNotEquals(secret1, secret2);
        assertNotNull(secret1);
    }

    // Read Secret
    @Test(expected = IllegalArgumentException.class)
    public void test_VA_read_nullUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret(null, secret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_read_nullSecret() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Bob", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_read_emptyUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("", secret);
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_read_notShare() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Carl", secret);
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_read_unShared() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Bob", secret);
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_read_unshareIndirect() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        secretService.unshareSecret("Alice", secret, "Carl");
        String content = secretService.readSecret("Carl", secret);
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_read_secretNotExist() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        UUID fault = UUID.randomUUID();
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Bob", fault);
    }

    @Test
    public void test_read_owner() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Alice", secret);
        assertNotNull(content);
    }

    @Test
    public void test_read_shared() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        String content = secretService.readSecret("Carl", secret);
        assertEquals("This is a test secret.", content);
    }

    @Test
    public void test_read_sharedAfterUnshare() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, "Bob");
        secretService.shareSecret("Alice", secret, "Bob");
        String content = secretService.readSecret("Bob", secret);
        assertNotNull(content);
    }

    // Share secret
    @Test(expected = IllegalArgumentException.class)
    public void test_VA_share_emptyUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_share_nullUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret(null, secret, "Bob");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_share_nullSecret() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", null, "Bob");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_share_sameUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Bob");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_share_unread() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.shareSecret("Bob", secret, "Carl");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_share_secretNotExist() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        UUID fault = UUID.randomUUID();
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", fault, "Carl");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_share_readBeforeUnshare() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.unshareSecret("Alice", secret, "Bob");
        secretService.shareSecret("Bob", secret, "Carl");
    }

    @Test
    public void test_share_owner() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
    }

    @Test
    public void test_share_read() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
    }


    // Unshare secret
    @Test(expected = IllegalArgumentException.class)
    public void test_VA_unshare_self() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, "Alice");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_unshare_nullUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_unshare_emptyUser() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("", secret, "Bob");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_VA_unshare_nullSecret() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", null, "Bob");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_unshare_notOwner() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        secretService.unshareSecret("Bob", secret, "Carl");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_unshare_secretNotExist() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        UUID fault = UUID.randomUUID();
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", fault, "Bob");
    }

    @Test(expected = NotAuthorizedException.class)
    public void test_AC_unshare_notShared() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, "Carl");
    }

    @Test
    public void test_unshare_direct() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.unshareSecret("Alice", secret, "Bob");
    }

    @Test
    public void test_unshare_indirect() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        secretService.unshareSecret("Alice", secret, "Carl");
    }

    // secret stats
    // reset stats
    @Test
    public void test_stats_reset() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        UUID secret2 = secretService.createSecret("Carl", "This is a test secret.");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        stats.resetStatsAndSystem();
        assertEquals(0, stats.getLengthOfLongestSecret());
        assertNull(stats.getMostTrustedUser());
        assertNull(stats.getWorstSecretKeeper());
        assertNull(stats.getBestKnownSecret());
    }

    @Test
    public void test_stats_reset_initial() throws IOException {
        assertEquals(0, stats.getLengthOfLongestSecret());
        assertNull(stats.getMostTrustedUser());
        assertNull(stats.getWorstSecretKeeper());
        assertNull(stats.getBestKnownSecret());
    }

    // length of the longest secret
    @Test
    public void test_stats_longestLength() throws IOException {
        secretService.createSecret("Alice", "This is a test secret.");
        secretService.createSecret("Bob", "This is the longest secret for test.");
        secretService.createSecret("Alice", "Another secret.");
        assertEquals(36, stats.getLengthOfLongestSecret());
    }

    // the most trusted user
    @Test
    public void test_stats_mostTrustedUser() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "This is a test secret.");
        UUID secret2 = secretService.createSecret("Alice", "The second secret.");
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Alice", secret2, "Bob");
        secretService.readSecret("Bob", secret1);
        secretService.shareSecret("Bob", secret1, "Carl");
        UUID secret3 = secretService.createSecret("Bob", "The third secret.");
        secretService.shareSecret("Bob", secret3, "Carl");
        secretService.shareSecret("Alice", secret2, "Bob");
        secretService.shareSecret("Alice", secret1, "Carl");
        assertEquals("Carl", stats.getMostTrustedUser());
    }

    @Test
    public void test_stats_mostTrustedUser_tie_unshare() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "This is a test secret.");
        UUID secret2 = secretService.createSecret("Alice", "The second secret.");
        secretService.shareSecret("Alice", secret1, "Carl");
        secretService.shareSecret("Alice", secret1, "Carl");
        secretService.shareSecret("Alice", secret2, "Carl");
        secretService.readSecret("Carl", secret1);
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Carl", secret1, "Bob");
        secretService.unshareSecret("Alice", secret1, "Bob");
        assertEquals("Bob", stats.getMostTrustedUser());
    }

    @Test
    public void test_stats_mostTrustedUser_cycle() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.shareSecret("Bob", secret, "Carl");
        secretService.readSecret("Carl", secret);
        secretService.shareSecret("Carl", secret, "Alice");
        secretService.shareSecret("Bob", secret, "Alice");
        assertEquals("Alice", stats.getMostTrustedUser());
    }

    // the worst secret keeper
    @Test
    public void test_stats_worstSecretKeeper() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "This is a test secret.");
        UUID secret2 = secretService.createSecret("Alice", "Another secret.");
        secretService.shareSecret("Alice", secret1, "Carl");
        secretService.shareSecret("Alice", secret2, "Carl");
        secretService.readSecret("Carl", secret1);
        secretService.shareSecret("Carl", secret1, "Alice");
        secretService.shareSecret("Carl", secret1, "Bob");
        secretService.readSecret("Bob", secret1);
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Alice", secret2, "Bob");
        secretService.shareSecret("Bob", secret1, "Carl");
        assertEquals("Carl", stats.getWorstSecretKeeper()); // Alice: 0/1; Bob 1/3; Carl: 2/3
    }

    @Test
    public void test_stats_worstSecretKeeper_tieUnshareDuplicate() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "This is a test secret.");
        UUID secret2 = secretService.createSecret("Alice", "Another secret.");
        secretService.shareSecret("Alice", secret1, "Carl");
        secretService.shareSecret("Alice", secret2, "Carl");
        secretService.readSecret("Carl", secret1);
        secretService.shareSecret("Carl", secret1, "Alice");
        secretService.shareSecret("Carl", secret1, "Bob");
        secretService.shareSecret("Carl", secret1, "Bob");
        secretService.shareSecret("Carl", secret1, "Bob");
        secretService.readSecret("Bob", secret1);
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Alice", secret2, "Bob");
        secretService.shareSecret("Bob", secret1, "Carl");
        secretService.shareSecret("Bob", secret1, "David");
        // tie above, no impact to result
        secretService.unshareSecret("Alice", secret2, "Carl");
        UUID secret3 = secretService.createSecret("Carl", "secret from Carl");
        secretService.shareSecret("Carl", secret3, "Alice");
        secretService.readSecret("Alice", secret3);
        secretService.shareSecret("Alice", secret3, "David");
        assertEquals("Bob", stats.getWorstSecretKeeper()); // Alice: 1/2; Bob 2/3; Carl: 2/3
    }

    @Test
    public void test_stats_worstSecretKeeper_zeroScore() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "David");
        assertEquals("David", stats.getWorstSecretKeeper()); // David: 0/1; Alice: undefined
    }

    // the best known secret
    @Test
    public void test_stats_bestKnown_onlyCreator() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.readSecret("Alice", secret);
        assertNull(stats.getBestKnownSecret());
    }

    @Test
    public void test_stats_bestKnown_duplicateUnshare() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "a test secret");
        UUID secret2 = secretService.createSecret("Bob", "test secret");
        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.readSecret("Bob", secret1);
        secretService.shareSecret("Alice", secret1, "Carl");
        secretService.readSecret("Carl", secret1);
        secretService.shareSecret("Bob", secret1, "Carl");
        secretService.readSecret("Carl", secret1);

        secretService.shareSecret("Bob", secret2, "Carl");
        secretService.shareSecret("Bob", secret2, "David");
        secretService.shareSecret("Bob", secret2, "Emily");
        secretService.readSecret("Carl", secret2);
        secretService.readSecret("David", secret2);
        secretService.readSecret("Emily", secret2);
        secretService.unshareSecret("Bob", secret2, "Emily");
        secretService.unshareSecret("Bob", secret2, "David");
        // secret1: 2; secret2: 3
        assertEquals(secret2, stats.getBestKnownSecret());
    }

    @Test
    public void test_stats_bestKnown_tie() throws IOException {
        UUID secret1 = secretService.createSecret("Alice", "Test secret");
        UUID secret2 = secretService.createSecret("Bob", "A test secret");

        secretService.shareSecret("Alice", secret1, "Bob");
        secretService.shareSecret("Alice", secret1, "David");
        secretService.shareSecret("Alice", secret1, "Emily");
        secretService.readSecret("Bob", secret1);
        secretService.readSecret("David", secret1);
        secretService.shareSecret("Bob", secret1, "David");
        secretService.readSecret("David", secret1);

        secretService.shareSecret("Bob", secret2, "Carl");
        secretService.readSecret("Carl", secret2);
        secretService.shareSecret("Carl", secret2, "David");
        secretService.readSecret("David", secret2);
        secretService.unshareSecret("Bob", secret2, "Carl");
        secretService.unshareSecret("Bob", secret2, "David");

        // secret1: 2; secret2: 2
        assertEquals(secret2, stats.getBestKnownSecret());
    }

    // IOException retry
    @Test(expected = IOException.class)
    public void test_retry() throws IOException {
        UUID secret = secretService.createSecret("Alice", "test secret");
        secretService.shareSecret("Alice", secret, "Bob");
    }

}