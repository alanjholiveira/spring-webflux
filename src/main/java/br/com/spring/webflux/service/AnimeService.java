package br.com.spring.webflux.service;

import br.com.spring.webflux.domain.Anime;
import br.com.spring.webflux.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return repository.saveAll(animes)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime anime) {
        if (StringUtil.isNullOrEmpty(anime.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
        }
    }

    public Mono<Void> update(Anime anime) {
        return findById(anime.getId())
                .flatMap(repository::save)
                .then();
    }

    private <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<Void> delete(int id) {
        return findById(id)
                .flatMap(repository::delete);
    }

}
