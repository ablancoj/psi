package cat.urv.crises.eigenpaillier.paillier;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Paillier {

    public static KeyPair Keygen(int bitlength) {
        SecureRandom rng = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(bitlength/2, rng);
        BigInteger q = p;
        while (p.equals(q)) {
            q = BigInteger.probablePrime(bitlength/2, rng);
        }

        BigInteger n = p.multiply(q);
        BigInteger lambda = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger mu = lambda.modInverse(n);

        PublicKey pk = new PublicKey(n);
        SecretKey sk = new SecretKey(lambda, mu);
        return new KeyPair(pk, sk);
    }

    public static EncryptedInteger encrypt(PublicKey pk, BigInteger plaintext) {
        SecureRandom rng = new SecureRandom();
        BigInteger r = new BigInteger(pk.getN().bitLength(), rng);
        while (r.compareTo(pk.getN()) == 1) {
            r = new BigInteger(pk.getN().bitLength(), rng);
        }
        BigInteger c1 = pk.getG().modPow(plaintext,pk.getN2());
        BigInteger c2 = r.modPow(pk.getN(), pk.getN2());
        BigInteger c = c1.multiply(c2).mod(pk.getN2());
        return new EncryptedInteger(c, pk);
    }

    public static BigInteger decrypt(SecretKey sk, EncryptedInteger ciphertext) {
        PublicKey pk = ciphertext.getPublic();
        BigInteger c = ciphertext.getValue();
        BigInteger x = c.modPow(sk.getLambda(), pk.getN2());
        x = (x.subtract(BigInteger.ONE)).divide(pk.getN());
        return x.multiply(sk.getMu()).mod(pk.getN());
    }
}
