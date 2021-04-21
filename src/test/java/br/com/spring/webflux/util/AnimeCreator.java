package br.com.spring.webflux.util;

import br.com.spring.webflux.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("Tensei Shitara Slime Datta Ken")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1)
                .name("Tensei Shitara Slime Datta Ken")
                .build();
    }

    public static Anime createdValidUpdatedAnime() {
        return Anime.builder()
                .id(1)
                .name("Tensei Shitara Slime Datta Ken 2")
                .build();
    }
}
