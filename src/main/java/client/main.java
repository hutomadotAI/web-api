package client;

import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

/**
 * Created by mauriziocibelli on 26/04/16.
 */
public class main {

    public static void main(String[] args) {


       hutoma.api.server.AWS.SQS.push_msg(String.valueOf(msg.ready_for_deep_training));
        String encoding_key="L0562EMBfnLadKy57nxo9btyi3BEKm9m+DoNvGcfZa+DjHsXwTl+BwCE4NeKEAagfkhYBFvhvJoAgtugSsQOfw==";

        String mybearer="eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8c4-nq5-If5-PpFKOkqOnp4uQAlDI2MTUyC3uDQJyMsoLcnPTdRLLMhUqgUAAAD__w.t45-MMriJ2fDqrhwd8ccqqtG19KhuDNJpzN_d-HSHvA";
        String devkey ="eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA";
        String admin ="eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c";

        try {
        String s = Jwts.builder()
                .claim("ROLE", Role.ROLE_ADMIN)
                .setSubject("admin")
                .compressWith(CompressionCodecs.DEFLATE) // or CompressionCodecs.GZIP
                .signWith(SignatureAlgorithm.HS256, encoding_key)
                .compact();



            assert Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(mybearer).getBody().getSubject().equals("hutoma.api");

            String body =  Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(mybearer).getBody().getSubject();
            Jws<Claims> c = Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(mybearer);
            System.out.print(c.getBody().get("ROLE"));
            boolean b = Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(mybearer).getBody().getSubject().equals("ABC");


        System.out.print(s);
    } catch (SignatureException e) {

        System.out.println("error");
    }
    }
}
