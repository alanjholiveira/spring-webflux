package br.com.spring.webflux.service;

import br.com.spring.webflux.domain.Anime;
import br.com.spring.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository repository;

    public Flux<Anime> findAll() {
        return repository.findAll();
    }

    public Mono<Anime> findById(int id) {
        return repository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException())
                .log();
    }

    public Mono<Anime> save(Anime anime) {
        return repository.save(anime);
    }

    public Mono<Void> update(Anime anime) {
        return repository.findById(anime.getId())
                .map(result -> anime.withId(result.getId()))
                .flatMap(repository::save)
                .thenEmpty(Mono.empty());
    }

    private <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

}
