package cat.urv.crises.eigenpaillier.paillier;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

/**
 * Created by Alberto Blanco on 23/03/2017.
 */
public class PublicKey {
    private BigInteger n;
    private BigInteger n2;
    private BigInteger g;

    public PublicKey(BigInteger n) {
        this.n = n;
        this.n2 = n.multiply(n);
        this.g = n.add(BigInteger.ONE);
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getN2() {
        return n2;
    }

    public BigInteger getG() {
        return g;
    }

    public boolean equals(PublicKey b) {
        return (this.n.equals(b.n) && this.g.equals(b.g));
    }

    public String serialize() {
        return Base64.encodeBase64URLSafeString(n.toByteArray());
    }

    public static PublicKey load(String base64encoding) {
        BigInteger n = new BigInteger(1, Base64.decodeBase64(base64encoding));
        return new PublicKey(n);
    }
}
