package ti.gateway.kubernetes.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@JwtKeyEnabled
public class TokenVerifier {
    private final Logger log = LoggerFactory.getLogger(JwtKeyGatewayFilterFactory.class);

    TokenVerifier() {
    }

    public boolean verify(String token, Object key) {
        if (key instanceof PublicKey) {
            return this.verifyRSA(token, (PublicKey)key);
        } else if (key instanceof String) {
            return this.verifyMAC(token, (String)key);
        } else {
            throw new IllegalStateException("Unsupported key class: " + key.getClass());
        }
    }

    private boolean verifyRSA(String token, PublicKey publicKey) {
        if (!publicKey.getAlgorithm().equals("RSA")) {
            throw new RuntimeException("Unsupported key algorithm");
        } else {
            try {
                RSASSAVerifier rsassaVerifier = new RSASSAVerifier((RSAPublicKey)publicKey);
                JWSObject tokenParsed = JWSObject.parse(token);
                return this.tokenIsNotExpired(tokenParsed) && tokenParsed.verify(rsassaVerifier);
            } catch (JOSEException | ParseException exception) {
                this.log.error("Error parsing and/or verifying JWT token.", exception);
                return false;
            }
        }
    }

    private boolean verifyMAC(String token, String privateKey) {
        if (!StringUtils.hasText(privateKey)) {
            throw new IllegalArgumentException("Private key is blank");
        } else {
            try {
                JWSVerifier v = new MACVerifier(privateKey);
                SignedJWT t = SignedJWT.parse(token);
                return this.tokenIsNotExpired(t) && t.verify(v);
            } catch (JOSEException | ParseException exception) {
                this.log.error("Error verifying MAC-signed token", exception);
                return false;
            }
        }
    }

    private boolean tokenIsNotExpired(JWSObject tokenParsed) {
        Instant now = ZonedDateTime.now(ZoneId.of("UTC")).toInstant();
        Long exp = (Long)tokenParsed.getPayload().toJSONObject().get("exp");
        Instant expirationTime = Instant.ofEpochSecond(exp);
        return now.isBefore(expirationTime);
    }


}
