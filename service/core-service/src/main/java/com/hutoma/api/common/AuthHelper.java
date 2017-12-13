package com.hutoma.api.common;

import com.hutoma.api.access.InvalidRoleException;
import com.hutoma.api.access.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.util.UUID;

public abstract class AuthHelper {

    private static final String ROLE_CLAIM_NAME = "ROLE";

    public static String generateDevToken(final UUID devId, final String securityRole, final String encodingKey) {
        return Jwts.builder()
                .claim(ROLE_CLAIM_NAME, securityRole)
                .setSubject(devId.toString())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, encodingKey)
                .compact();
    }

    /**
     * Gets the claims from the JWT token.
     * @param token  the token as a string
     * @param encodingKey the encoding key
     * @return the claims
     */
    public static Claims getClaimsFromToken(final String token, final String encodingKey) {
        return Jwts.parser().setSigningKey(encodingKey).parseClaimsJws(token).getBody();
    }

    public static String getSubjectFromToken(final String token, final String encodingKey) {
        return getClaimsFromToken(token, encodingKey).getSubject();
    }

    public static String getRoleStringFromClaims(final Claims claims) {
         return claims.get("ROLE").toString();
    }

    public static Role getRoleFromClaims(final Claims claims) throws InvalidRoleException {
        String stringRole = getRoleStringFromClaims(claims);
        return Role.fromString(stringRole);
    }
}
