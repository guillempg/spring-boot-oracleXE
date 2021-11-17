package com.example.springjpaoracle.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ResourceOwnerOrGrantedAuthority
{
    private final Supplier<Authentication> authenticationSupplier;

    public ResourceOwnerOrGrantedAuthority(final Supplier<Authentication> authenticationSupplier)
    {
        this.authenticationSupplier = authenticationSupplier;
    }

    public boolean validate(String resourcePath, String... requiredAuthorities)
    {
        var authentication = authenticationSupplier.get();
        Stream<String> requesterAuthorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
        boolean isOwner = resourcePath.equals(authentication.getName());
        boolean hasPermission = requesterAuthorities.anyMatch(Arrays.asList(requiredAuthorities)::contains);
        return isOwner || hasPermission;
    }
}
