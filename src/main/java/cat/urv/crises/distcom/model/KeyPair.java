package cat.urv.crises.distcom.model;

import java.io.Serializable;

import thep.paillier.PrivateKey;
import thep.paillier.PublicKey;


public class KeyPair implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private PrivateKey mPrivateKey;
	private PublicKey mPublicKey;
	
	public KeyPair(int key_length) {
		mPrivateKey = new PrivateKey(key_length);
		mPublicKey = mPrivateKey.getPublicKey();
	}
	
	public KeyPair(PrivateKey sk, PublicKey pk) {
		mPrivateKey = sk;
		mPublicKey = pk;
	}
	
	public PrivateKey getPrivateKey() {
		return mPrivateKey;
	}
	
	public PublicKey getPublicKey() {
		return mPublicKey;
	}
}
