package br.com.spring.webflux.service;

import br.com.spring.webflux.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository repository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByUsername(username)
                .cast(UserDetails.class);
    }
}
