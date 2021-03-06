package com.example.springjpaoracle.authorization;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Converts Keycloak Jwt resource_access.springjpaoracle.roles into a Collection<GrantedAuthority>
 **/
public class CustomJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>>
{
    @Override
    public Collection<GrantedAuthority> convert(final Jwt source)
    {
        final Collection<GrantedAuthority> output = new ArrayList<>();
        final Map<String, Object> resources = source.getClaimAsMap("resource_access");
        final JSONObject springjpaoracle = (JSONObject) resources.get("springjpaoracle");
        final JSONArray roles = (JSONArray) springjpaoracle.get("roles");
        roles.forEach(v -> output.add(new SimpleGrantedAuthority(v.toString())));
        return output;
    }
}
