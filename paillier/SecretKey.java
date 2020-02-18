package cat.urv.crises.eigenpaillier.paillier;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

/**
 * Created by Alberto Blanco on 23/03/2017.
 */
public class SecretKey {
    private BigInteger lambda;
    private BigInteger mu;

    public SecretKey(BigInteger lambda, BigInteger mu) {
        this.lambda = lambda;
        this.mu = mu;
    }

    public BigInteger getLambda() {
        return lambda;
    }

    public BigInteger getMu() {
        return mu;
    }

    public String serialize() {
        return Base64.encodeBase64URLSafeString(lambda.toByteArray());
    }

    public static SecretKey load(String base64encoding, PublicKey pk) {
       BigInteger lambda = new BigInteger(1, Base64.decodeBase64(base64encoding));
       BigInteger mu = lambda.modInverse(pk.getN());
       return new SecretKey(lambda, mu);
    }
}
