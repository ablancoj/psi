package cat.urv.crises.eigenpaillier.paillier;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Alberto Blanco on 23/03/2017.
 */
public class KeyPair {
    private PublicKey pk;
    private SecretKey sk;

    public KeyPair(PublicKey pk, SecretKey sk) {
        this.pk = pk;
        this.sk = sk;
    }

    public PublicKey getPublic() {
        return pk;
    }

    public SecretKey getSecret() {
        return sk;
    }

    public String serialize() {
        String pk_enc = pk.serialize();
        String sk_enc = sk.serialize();
        return pk_enc + "," + sk_enc;
    }

    public static KeyPair load(String encoding) {
        String[] enc = encoding.split(",");
        PublicKey pk = PublicKey.load(enc[0]);
        SecretKey sk = SecretKey.load(enc[1], pk);
        return new KeyPair(pk, sk);
    }
}
