package masecla.modrinth4j.client;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.SneakyThrows;
import masecla.modrinth4j.data.DataUtil;
import masecla.modrinth4j.endpoints.SearchEndpoint.SearchRequest;
import masecla.modrinth4j.endpoints.SearchEndpoint.SearchResponse;
import masecla.modrinth4j.endpoints.project.ModifyProject.ProjectModifications;
import masecla.modrinth4j.endpoints.project.ProjectEndpoints;
import masecla.modrinth4j.endpoints.project.gallery.CreateGalleryImage.CreateGalleryImageRequest;
import masecla.modrinth4j.endpoints.project.gallery.ModifyGalleryImage.ModifyGalleryImageRequest;
import masecla.modrinth4j.environment.EnvReader;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.project.Project;
import masecla.modrinth4j.model.project.ProjectDonationPlatform;
import masecla.modrinth4j.model.project.ProjectGalleryImage;
import masecla.modrinth4j.model.project.SupportStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link ProjectEndpoints} class.
 */
public class ProjectEndpointsTests {
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
     * This method tests the search endpoint.
     */
    @Test
    public void testSearch() {
        // We're looking for the Gravestones mod
        SearchResponse response = client.search(SearchRequest.builder().query("fabric").build()).join();

        assertTrue(
            Arrays.stream(response.getHits()).anyMatch(c -> c.getSlug().equals("gravestones")),
            "The search response did not contain the mod 'Gravestones'!"
        );
    }

    /**
     * This method tests changing the icon of a project.
     */
    @Test
    @SneakyThrows
    public void testIconChange() {
        Project prj = DataUtil.createSampleProject(client);
        String iconPath = getClass().getClassLoader().getResource("icon.png").getFile();

        client.projects().changeProjectIcon(prj.getSlug(), new File(iconPath)).join();

        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests checking for slug availability.
     */
    @Test
    public void testSlugAvailability() {
        assertTrue(
            client.projects().checkSlugAvailability("modrinth4j-test-project").join(),
            "The slug 'modrinth4j-test-project' is not available!"
        );
        assertFalse(
            client.projects().checkSlugAvailability("gravestones").join(),
            "The slug 'gravestones' is available!"
        );
    }

    /**
     * This method tests converting a slug to an ID.
     */
    @Test
    public void testGetIdBySlug() {
        Project prj = DataUtil.createSampleProject(client);
        Project fetched = client.projects().get(prj.getSlug()).join();

        assertEquals(
            client.projects().getProjectIdBySlug(fetched.getSlug()).join(),
            fetched.getId()
        );

        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests creating a project.
     */
    @Test
    public void testCreate() {
        Project prj = DataUtil.createSampleProject(client);
        assertNotNull(prj, "The project was not created!");
        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests creating gallery images.
     */
    @Test
    public void testGalleryCreate() {
        Project prj = DataUtil.createSampleProject(client);
        client.projects().createGalleryImage(prj.getSlug(),
                CreateGalleryImageRequest.builder().featured(true)
                        .title("Test Image").description("This is a test image")
                        .file(DataUtil.getImage()).build())
                .join();

        prj = DataUtil.fetchSampleProject(client);

        assertTrue(prj.getGallery().size() > 0, "The project did not have any gallery images!");
        ProjectGalleryImage img = prj.getGallery().get(0);

        assertTrue(
            img.isFeatured(),
            "The image was not featured!"
        );
        assertEquals(
            "Test Image", img.getTitle(),
            "The image did not have the correct title!"
        );
        assertEquals(
            "This is a test image", img.getDescription(),
            "The image did not have the correct description!"
        );
        assertTrue(
            DataUtil.verifyIdentical(img.getUrl(), DataUtil.getImage()),
            "The image is not the one provided"
        );

        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests deleting a gallery image.
     */
    @Test
    public void testDeleteGalleryImage() {
        Project prj = DataUtil.createSampleProject(client);
        client.projects().createGalleryImage(prj.getSlug(),
                CreateGalleryImageRequest.builder().featured(true)
                        .title("Test Image").description("This is a test image")
                        .file(DataUtil.getImage()).build())
                .join();
        prj = DataUtil.fetchSampleProject(client);

        client.projects().deleteGalleryImage(prj.getSlug(), prj.getGallery().get(0).getUrl()).join();

        prj = DataUtil.fetchSampleProject(client);
        assertTrue(prj.getGallery().isEmpty(), "The project still has gallery images!");

        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests modifying a gallery image.
     */
    @Test
    public void testGalleryModify() {
        Project prj = DataUtil.createSampleProject(client);
        client.projects().createGalleryImage(prj.getSlug(),
                CreateGalleryImageRequest.builder().featured(true)
                        .title("Test Image").description("This is a test image")
                        .file(DataUtil.getImage()).build())
                .join();
        prj = DataUtil.fetchSampleProject(client);

        client.projects().modifyGalleryImage(prj.getSlug(),
                ModifyGalleryImageRequest.builder().featured(false)
                        .title("Test Image 2").description("This is a test image 2")
                        .url(prj.getGallery().get(0).getUrl()).build())
                .join();

        prj = DataUtil.fetchSampleProject(client);
        ProjectGalleryImage img = prj.getGallery().get(0);

        assertFalse(
            img.isFeatured(),
            "The image was still featured!"
        );
        assertEquals(
            "Test Image 2", img.getTitle(),
            "The image did not have the correct title!"
        );
        assertEquals(
            "This is a test image 2", img.getDescription(),
            "The image did not have the correct description!"
        );
        assertTrue(
            DataUtil.verifyIdentical(img.getUrl(), DataUtil.getImage()),
            "The image is not the one provided"
        );

        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests deleting a project.
     */
    @Test
    public void testProjectDelete() {
        Project prj = DataUtil.createSampleProject(client);
        client.projects().delete(prj.getSlug()).join();
        assertNull(client.projects().get(prj.getSlug()).join(), "The project was not deleted!");
    }

    /**
     * This method tests fetching a project.
     */
    @Test
    public void testGetSingle() {
        Project prj = DataUtil.createSampleProject(client);
        Project fetched = client.projects().get(prj.getSlug()).join();
        assertNotNull(fetched, "The project was not fetched!");
        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests fetching multiple projects.
     */
    @Test
    public void testGetMultiple() {
        Project prj = DataUtil.createSampleProject(client);
        List<Project> fetched = client.projects().get(Arrays.asList(prj.getId(), "AULzIar5")).join();
        assertEquals(2, fetched.size(), "The project was not fetched!");
        DataUtil.deleteSampleProject(client);
    }

    /**
     * This method tests following and unfollowing projects.
     */
    @Test
    public void testFollowUnfollow() {
        var id = "AULzIar5";
        client.projects().followProject(id).join();
        var follows = client.users().getUserFollowedProjects(client.users().getSelf().join().getId())
                .join();

        assertTrue(follows.stream().anyMatch(c -> c.getId().equals(id)), "The project was not followed!");
        client.projects().unfollowProject(id).join();

        follows = client.users().getUserFollowedProjects(client.users().getSelf().join().getId()).join();
        assertFalse(follows.stream().anyMatch(c -> c.getId().equals(id)), "The project was not unfollowed!");
    }

    /**
     * This method tests fetching a project's dependencies.
     */
    @Test
    public void testProjectDependencies() {
        assertTrue(
            client.projects().getProjectDependencies("gravestones").join().getProjects().isEmpty(),
            "The project 'gravestones' has dependencies?"
        );
    }

    /**
     * This method tests modifying a project.
     */
    @Test
    public void testModifyProject() {
        Project prj = DataUtil.createSampleProject(client);

        client.projects().modify(prj.getId(), ProjectModifications.builder()
                .additionalCategories(List.of("cursed"))
                .body("Different body")
                .categories(List.of("adventure"))
                .clientSide(SupportStatus.UNSUPPORTED)
                .description("Different description")
                .discordUrl("https://discord.gg/1234")
                .donationUrls(
                    Collections.singletonList(
                        ProjectDonationPlatform.builder().id("other")
                            .url("https://example.com/donate").platform("other")
                            .build()
                    )
                )
                .issuesUrl("https://example.com/issues")
                .serverSide(SupportStatus.UNSUPPORTED)
                .slug("diff-slug-too")
                .sourceUrl("https://example.com/source")
                .title("Different title")
                .wikiUrl("https://example.com/wiki")
                .build()).join();

        prj = client.projects().get("diff-slug-too").join();

        assertTrue(
            prj.getAdditionalCategories().stream().anyMatch(c -> c.equals("cursed")),
            "The project did not have the correct additional categories!"
        );
        assertEquals(
            "Different body", prj.getBody(),
            "The project did not have the correct body!"
        );
        assertTrue(
            prj.getCategories().stream().anyMatch(c -> c.equals("adventure")),
            "The project did not have the correct categories!"
        );
        assertSame(
            SupportStatus.UNSUPPORTED, prj.getClientSide(),
            "The project did not have the correct client side support status!"
        );
        assertEquals(
            "Different description", prj.getDescription(),
            "The project did not have the correct description!"
        );
        assertEquals(
            "https://discord.gg/1234", prj.getDiscordUrl(),
            "The project did not have the correct discord url!"
        );
        assertTrue(
            prj.getDonationUrls().stream().anyMatch(c -> c.getUrl().equals("https://example.com/donate")),
            "The project did not have the correct donation urls!"
        );
        assertEquals(
            "https://example.com/issues", prj.getIssuesUrl(),
            "The project did not have the correct issues url!"
        );
        assertSame(
            SupportStatus.UNSUPPORTED, prj.getServerSide(),
            "The project did not have the correct server side support status!"
        );
        assertEquals(
            "diff-slug-too", prj.getSlug(),
            "The project did not have the correct slug!"
        );
        assertEquals(
            "https://example.com/source", prj.getSourceUrl(),
            "The project did not have the correct source url!"
        );
        assertEquals(
            "Different title", prj.getTitle(),
            "The project did not have the correct title!"
        );
        assertEquals(
            "https://example.com/wiki", prj.getWikiUrl(),
            "The project did not have the correct wiki url!"
        );

        client.projects().delete("diff-slug-too").join();
    }

    @Test
    public void testModifyProjectIdenticalSlug() {
        Project prj = DataUtil.createSampleProject(client);

        client.projects().modify(prj.getId(), ProjectModifications.builder()
                .slug(prj.getSlug())
                .build()).join();

        prj = client.projects().get(prj.getSlug()).join();

        assertEquals("modrinth4j-test-project", prj.getSlug(), "The project did not have the correct slug!");

        client.projects().delete("modrinth4j-test-project").join();
    }
}
