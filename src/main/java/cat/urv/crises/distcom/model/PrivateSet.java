package cat.urv.crises.distcom.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;

import thep.paillier.EncryptedInteger;
import thep.paillier.EncryptedPolynomial;
import thep.paillier.PrivateKey;
import thep.paillier.PublicKey;
import thep.paillier.exceptions.BigIntegerClassNotValid;
import thep.paillier.exceptions.PublicKeysNotEqualException;


public class PrivateSet implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final Polynomial X = new Polynomial(BigInteger.ONE, 1);
	
	private final EncryptedPolynomial mPrivateSet;
	
	public PrivateSet(BigInteger[] elements, PublicKey pk) throws BigIntegerClassNotValid {
		Polynomial acc = new Polynomial(BigInteger.ONE, 0);
		for (BigInteger i : elements) {
			acc = acc.times(X.minus(new Polynomial(i, 0)));
		}
		mPrivateSet = new EncryptedPolynomial(acc.coef, pk);
	}
	
	public PrivateSet(EncryptedPolynomial ep) {
		mPrivateSet = ep;
	}
	
	public EncryptedInteger[] cardinalityIntersection(BigInteger[] publicSet, BigInteger crs) throws BigIntegerClassNotValid, PublicKeysNotEqualException {
		EncryptedInteger[] test = new EncryptedInteger[publicSet.length];
		SecureRandom rand = new SecureRandom();
		byte[] randomBits = new byte[mPrivateSet.getPublicKey().getBits() / 8];
		for (int i = 0; i < publicSet.length; i++) {
			EncryptedInteger t = mPrivateSet.fastEvaluate(publicSet[i]);
			rand.nextBytes(randomBits);
			BigInteger randomNumber = new BigInteger(randomBits);
			test[i] = t.multiply(randomNumber).add(new EncryptedInteger(crs, mPrivateSet.getPublicKey()));
		}
		return test;
	}
	
	public EncryptedInteger[] intersection(BigInteger[] publicSet) throws BigIntegerClassNotValid, PublicKeysNotEqualException {
		EncryptedInteger[] test = new EncryptedInteger[publicSet.length];
		SecureRandom rand = new SecureRandom();
		byte[] randomBits = new byte[mPrivateSet.getPublicKey().getBits() / 8];
		for (int i = 0; i < publicSet.length; i++) {
			EncryptedInteger t = mPrivateSet.fastEvaluate(publicSet[i]);
			rand.nextBytes(randomBits);
			BigInteger randomNumber = new BigInteger(randomBits);
			test[i] = t.multiply(randomNumber).add(new EncryptedInteger(publicSet[i], mPrivateSet.getPublicKey()));
		}
		return test;
	}
	
	public boolean inSet(BigInteger element, PrivateKey sk) throws BigIntegerClassNotValid, PublicKeysNotEqualException {
		return BigInteger.ZERO.equals(mPrivateSet.fastEvaluate(element).decrypt(sk));
	}
}
