package ti.gateway.kubernetes.jwt;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Component
@JwtKeyEnabled
public class KeyParser {
    private static final Pattern HS_ALG_PATTERN = Pattern.compile("^hs\\d{3}$", 2);

    KeyParser() {
    }

    public Object parse(String alg, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (!"RSA".equals(alg) && !"RS256".equals(alg) && !"RS384".equals(alg) && !"RS512".equals(alg)) {
            if (HS_ALG_PATTERN.matcher(alg).matches()) {
                return key;
            } else {
                throw this.NoSuchAlgorithmException(alg);
            }
        } else {
            return this.getRsaPublicKey(key);
        }
    }

    private RSAPublicKey getRsaPublicKey(String content) throws InvalidKeyException {
        try {
            if (content.contains("-----BEGIN CERTIFICATE-----")) {
                CertificateFactory fact = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());

                RSAPublicKey rsaPublicKey;
                try {
                    X509Certificate cer = (X509Certificate)fact.generateCertificate(is);
                    rsaPublicKey = (RSAPublicKey)cer.getPublicKey();
                } catch (Throwable error) {
                    try {
                        is.close();
                    } catch (Throwable var6) {
                        error.addSuppressed(var6);
                    }

                    throw error;
                }

                is.close();
                return rsaPublicKey;
            }
        } catch (CertificateException | IOException | ClassCastException exception) {
            throw new InvalidKeyException(exception);
        }

        String sanitizedKey = content.replace("-----BEGIN PUBLIC KEY-----", "").replaceAll(System.lineSeparator(), "").replace("-----END PUBLIC KEY-----", "");
        byte[] decode = Base64.getDecoder().decode(sanitizedKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(decode));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException exception) {
            throw new InvalidKeyException(exception);
        }
    }

    private NoSuchAlgorithmException NoSuchAlgorithmException(String alg) {
        return this.NoSuchAlgorithmException(alg, (Exception)null);
    }

    private NoSuchAlgorithmException NoSuchAlgorithmException(String alg, Exception e) {
        return new NoSuchAlgorithmException("Unsupported algorithm: " + alg, e);
    }


}
