package br.com.spring.webflux.integration;

import br.com.spring.webflux.domain.Anime;
import br.com.spring.webflux.repository.AnimeRepository;
import br.com.spring.webflux.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    private final static String ADMIN_USER = "admin";
    private final static String REGULAR_USER = "user";

    @MockBean
    private AnimeRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install(builder ->
                builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
    }

    @BeforeEach
    void setup() {

        BDDMockito.when(repository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository
                .saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(repository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(repository.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    void blockHoundWorksTest() {
        try {
            FutureTask<?> task = new FutureTask<>( ()-> {
                Thread.sleep(0);
                return "";
            } );
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("listAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    @WithUserDetails(REGULAR_USER)
    void listAll_ReturnForbidden_WhenUserDoesNotHaveRoleAdmin() {
        webTestClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("listAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void listAll_ReturnFluxOfAnime_WhenSuccessful() {
        webTestClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                    .jsonPath("$.[0].id").isEqualTo(anime.getId())
                    .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("listAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {
        webTestClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns a Mono with anime when it exists and user is successfully authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    void findById_ReturnMonoAnime_WhenSuccessful() {
        webTestClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist and user is successfully authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("save creates an anime when successful and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        webTestClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void saveBatch_CreatesListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        webTestClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("saveBatch returns Mono error when one of the objects in the list contains empty or null name and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void saveBatch_ReturnsMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(repository
                .saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        webTestClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void save_ReturnsError_WhenNameIsEmpty() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        webTestClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

    }

    @Test
    @DisplayName("delete removes the anime when successful and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void delete_RemovesAnime_WhenSuccessful() {
        webTestClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void update_SaveUpdatedAnime_WhenSuccessful() {
        webTestClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

}
