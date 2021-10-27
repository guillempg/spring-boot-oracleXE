package com.example.springjpaoracle.auth0;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTParser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.text.ParseException;
import java.util.*;

/**
 * Converts Keycloak Jwt resource_access.springjpaoracle.roles into a Collection<GrantedAuthority>
 **/
public class CustomJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>>
{
    @Override
    public Collection<GrantedAuthority> convert(final Jwt source)
    {
        final Collection<GrantedAuthority> output = new ArrayList<>();
        try
        {
            final Map<String, Object> resources = source.getClaimAsMap("resource_access");
            final JSONObject springjpaoracle = (JSONObject) resources.get("springjpaoracle");
            final JSONArray roles = (JSONArray) springjpaoracle.get("roles");
            roles.forEach(v -> output.add(new SimpleGrantedAuthority(v.toString())));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }

    public static Set<String> getRoles(String token) throws ParseException
    {
        JWTParser.parse(token);
        final Jwt build = Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .build();
        final Map<String, Object> resources = build.getClaimAsMap("resource_access");
        final JSONObject springjpaoracle = (JSONObject) resources.get("springjpaoracle");
        final JSONArray rolesJson = (JSONArray) springjpaoracle.get("roles");

        Set<String> roles = new HashSet<>();
        rolesJson.forEach(json ->
        {
            final String s = json.toString();
            roles.add(s);
        });
        return roles;
    }
}
