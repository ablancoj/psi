package cat.urv.crises.distcom;

import java.math.BigInteger;

import thep.paillier.EncryptedInteger;
import thep.paillier.exceptions.BigIntegerClassNotValid;
import thep.paillier.exceptions.PublicKeysNotEqualException;
import cat.urv.crises.distcom.model.KeyPair;
import cat.urv.crises.distcom.model.PrivateSet;

public class Test {

	final static BigInteger crs = BigInteger.valueOf(1010101010);

	public static void main(String[] args) throws BigIntegerClassNotValid, PublicKeysNotEqualException {
		BigInteger[] setA = {a(1), a(2), a(3), a(4), a(5), a(6)};
		BigInteger[] setB = {a(2), a(4), a(6), a(8)};
		
		KeyPair kpA = new KeyPair(1024);
		
		PrivateSet privateSetA = new PrivateSet(setA, kpA.getPublicKey());
		
		EncryptedInteger[] test1 = privateSetA.cardinalityIntersection(setB, crs);
		EncryptedInteger[] test2 = privateSetA.intersection(setB);
		
		int card = 0;
		for (EncryptedInteger i: test1) {
			if (i.decrypt(kpA.getPrivateKey()).equals(crs)) {
				card++;
			}
		}
		System.out.println("Card: "+ card);
		
		
		System.out.print("Intersection: ");
		for (EncryptedInteger i: test2) {
			BigInteger j = i.decrypt(kpA.getPrivateKey());
			if (in(j,setA)) {
				System.out.println(i.decrypt(kpA.getPrivateKey()));
			}
		}
	}
	
	
	public static BigInteger a(long b) {
		return BigInteger.valueOf(b);
	}
	
	public static boolean in(BigInteger a, BigInteger[] set) {
		for (BigInteger i: set) {
			if (i.equals(a)) return true;
		}
		return false;
	}

}
