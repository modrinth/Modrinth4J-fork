package masecla.modrinth4j.client;

import masecla.modrinth4j.endpoints.tags.TagsEndpoints;

import masecla.modrinth4j.environment.EnvReader;
import masecla.modrinth4j.main.ModrinthAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link TagsEndpoints} class.
 */
public class TagsEndpointsTests {
    /** The client to be used */
    private static ModrinthAPI client;

    /**
     * Sets up the client.
     */
    @BeforeAll
    public static void setupClient() {
        EnvReader env = new EnvReader();
        client = ModrinthAPI.rateLimited(env.getAgent(), env.getStagingUrl(), env.getApiKey());

    }

    /**
     * This method tests getting all the categories.
     */
    @Test
    public void testGetCategories() {
        assertNotNull(client.tags().getCategories().join());
    }

    /**
     * This method tests getting all the donation platforms.
     */
    @Test
    public void testGetDonationPlatforms() {
        assertNotNull(client.tags().getDonationPlatforms().join());
    }

    /**
     * This method tests getting all the loaders.
     */
    @Test
    public void testGetLoaders() {
        assertNotNull(client.tags().getLoaders().join());
    }

    /**
     * This method tests getting all the game versions.
     */
    @Test
    public void testGetGameVersions() {
        assertNotNull(client.tags().getGameVersions().join());
    }

    /**
     * This method tests getting all the licenses.
     */
    @Test
    public void testGetLicenses() {
        assertNotNull(client.tags().getLicenses().join());
    }

    /**
     * This method tests getting all the mod types.
     */
    @Test
    public void testGetReportTypes() {
        assertNotNull(client.tags().getReportTypes().join());
    }
}
