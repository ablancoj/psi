package cat.urv.crises.eigenpaillier.paillier;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

/**
 * Created by Alberto Blanco on 23/03/2017.
 */
public class EncryptedInteger {
    private BigInteger value;
    private PublicKey pk;

    public EncryptedInteger(BigInteger value, PublicKey pk) {
        this.value = value;
        this.pk = pk;
    }

    public BigInteger getValue() {
        return value;
    }

    public PublicKey getPublic() {
        return pk;
    }

    public EncryptedInteger sum(EncryptedInteger b) /*throws Exception*/ {
       /*if (!pk.equals(b.pk)) {
           throw new Exception("Public keys do not match");
       }*/

       BigInteger c = value.multiply(b.value).mod(pk.getN2());
       return new EncryptedInteger(c, pk);
    }

    public EncryptedInteger sum(BigInteger m) {
        BigInteger c = pk.getG().modPow(m, pk.getN2());
        c = value.multiply(c).mod(pk.getN2());
        return new EncryptedInteger(c, pk);
    }

    public EncryptedInteger multiply(BigInteger m) {
        BigInteger c = value.modPow(m, pk.getN2());
        return new EncryptedInteger(c, pk);
    }

    public String serialize() {
        return Base64.encodeBase64URLSafeString(value.toByteArray());
    }

    public static EncryptedInteger load(String encoding, PublicKey pk) {
        BigInteger value = new BigInteger(1, Base64.decodeBase64(encoding));
        return new EncryptedInteger(value, pk);
    }
}
