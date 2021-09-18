package com.example.users.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {


    @Value("${auth.secret}")
    private String SECRET_KEY;
    @Value("${auth.expiration}")
    private long EXPIRATION = 604800L; // 7 * 24 * 60 * 60

    public String generateToken(UserDetails userDetails) {

        // generate claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("created", new Date());

        // generate expiration
        Date expiration = new Date(System.currentTimeMillis() + EXPIRATION * 1000);

        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        System.out.println("Generated token : "+token);

        return token;

    }


    public String getUserNameFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }

    private Claims getClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex){
            claims = null;
        }

        return claims;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);

        if(username.equals(userDetails.getUsername()) && !isTokenExpired(token))
            return true;

        return false;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaims(token).getExpiration();

        return expiration.before(new Date());
    }

}
