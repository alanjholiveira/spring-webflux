package br.com.spring.webflux.controller;

import br.com.spring.webflux.domain.Anime;
import br.com.spring.webflux.repository.AnimeRepository;
import br.com.spring.webflux.service.AnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("animes")
@Slf4j
public class AnimeController {

    private final AnimeService service;

    @GetMapping
    public Flux<Anime> listAll() {
        return service.findAll();
    }

    @GetMapping("{id}")
    public Mono<Anime> findById(@PathVariable int id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> save(@Valid @RequestBody Anime anime) {
        return service.save(anime);
    }

    @PutMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> update(@Valid @RequestBody Anime anime) {
        return service.update(anime);
    }

}
