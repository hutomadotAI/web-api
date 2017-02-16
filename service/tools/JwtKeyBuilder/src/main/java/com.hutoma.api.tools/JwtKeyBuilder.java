package com.hutoma.api.tools;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.util.Base64;

/**
 * Generates a random key to use in JWT.
 */
public class JwtKeyBuilder {

    // Generates a new secure-random 512 bit secret key suitable for creating and verifying HMAC signatures.
    private static final Key secret = MacProvider.generateKey(SignatureAlgorithm.HS512);
    // Returns the key in its primary encoding format
    private static final byte[] secretBytes = secret.getEncoded();
    // Base64 encode the secret
    private static final String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);

    public static void main(String[] args) {
        System.out.println(String.format("New secret base64 encoded bytes: \n-----\n%s\n-----\n", base64SecretBytes));
    }
}