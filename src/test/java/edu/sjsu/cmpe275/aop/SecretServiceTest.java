package edu.sjsu.cmpe275.aop;

import org.junit.Before;
import org.junit.Test;
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
    public void test_VA_create() throws IOException {
        UUID id = secretService.createSecret("Alice", "This is a test secret.");
        assertNotNull(id);
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
        String content = secretService.readSecret("Bob", secret);
        assertNotNull(content);
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
        secretService.shareSecret("Alice", secret, "Alice");
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

    @Test
    public void test_share_readBeforeUnshare() throws IOException {
        UUID secret = secretService.createSecret("Alice", "This is a test secret.");
        secretService.shareSecret("Alice", secret, "Bob");
        secretService.readSecret("Bob", secret);
        secretService.unshareSecret("Alice", secret, "Bob");
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

}