package br.com.spring.webflux.service;

import br.com.spring.webflux.domain.Anime;
import br.com.spring.webflux.repository.AnimeRepository;
import br.com.spring.webflux.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class AnimeServiceTest {

    @InjectMocks
    private AnimeService service;

    @Mock
    private AnimeRepository repository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    void setup() {
        BDDMockito.when(repository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository.save(AnimeCreator.createAnimeToBeSave()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository
                .saveAll(List.of(AnimeCreator.createAnimeToBeSave(), AnimeCreator.createAnimeToBeSave())))
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
    @DisplayName("findAll returns a flux of anime")
    void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(service.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(service.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    void findById_ReturnMonoError_WhenEmptyMonoReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSave();

        StepVerifier.create(service.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll creates a list of anime when successful")
    void saveAll_CreatesListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSave();

        StepVerifier.create(service.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns Mono error when on of the objects in the list contains null or empty name")
    void saveAll_ReturnsMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSave();

        BDDMockito.when(repository
                .saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        StepVerifier.create(service.saveAll(List.of(animeToBeSaved, animeToBeSaved.withName(""))))
                .expectSubscription()
                .expectNext(anime)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    void delete_RemovesAnime_WhenSuccessful() {
        StepVerifier.create(service.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete retuns Mono error when anime does not exist")
    void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    void update_SaveUpdateAnime_WhenSuccessful() {
        StepVerifier.create(service.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns Mono error whe anime does exist")
    void update_RetunsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

}
