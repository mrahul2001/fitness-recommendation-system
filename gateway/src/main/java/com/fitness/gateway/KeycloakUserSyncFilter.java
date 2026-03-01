package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userID = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest request = getUserDetails(token);

        if (userID == null) {
            userID = request.getKeycloakId();
        }

        if (userID != null && token != null) {
            try {
                String finalUserID = userID;
                return userService.validateUser(userID)
                        .flatMap(exist -> {
                            if (!exist) {

                                if (request != null) {
                                    return userService.registerUser(request)
                                            .then(Mono.empty());
                                } else {
                                    log.info("Unable to create/register User");
                                    return Mono.empty();
                                }
                            } else {
                                log.info("User doesn't exist");
                                return Mono.empty();
                            }
                        }).then(Mono.defer(() -> {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-ID", finalUserID)
                                    .build();
                            return chain.filter(
                                    exchange.mutate().request(mutatedRequest).build()
                            );
                        }));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return chain.filter(exchange);
    }

    public RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            RegisterRequest request = new RegisterRequest();
            request.setEmail(claimsSet.getClaimAsString("email"));
            request.setKeycloakId(claimsSet.getClaimAsString("sub"));
            request.setFirstName(claimsSet.getClaimAsString("given_name"));
            request.setLastName(claimsSet.getClaimAsString("family_name"));
            request.setPassword("Ranaghat@1");

            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
