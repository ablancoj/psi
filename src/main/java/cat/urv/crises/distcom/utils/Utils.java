package cat.urv.crises.distcom.utils;

import java.math.BigInteger;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Utils {
	
	public static final String KEY_ARRAY = "array";
	public static final String KEY_PUBLICKEY = "public_key";

	private Utils () {}
	
	@SuppressWarnings("unchecked")
	public static JSONObject PublicSetToJSON(BigInteger[] publicSet) {
		JSONArray array = new JSONArray();
		for (BigInteger i : publicSet) {
			array.add(Base64.encodeBase64String(i.toByteArray()));
		}
		JSONObject json = new JSONObject();
		json.put(KEY_ARRAY, array.toJSONString());
		return json;
	}
	
	public static BigInteger[] PublicSetfromJSON(JSONObject json) {
		JSONArray array = (JSONArray) json.get(KEY_ARRAY);
		BigInteger[] r = new BigInteger[array.size()];
		for (int i = 0; i < r.length; i++) {
			r[i] = new BigInteger(Base64.decodeBase64((String) array.get(i)));
		}
		return r;
	}
	
/*	@SuppressWarnings("unchecked")
	public static JSONObject PrivateSetToJSON(EncryptedInteger[] privateSet) {
		/* Only put values to array and append PK only once 
		JSONArray array = new JSONArray();
		for (EncryptedInteger i : privateSet) {
			array.add(Base64.encodeBase64String(i.getCipherVal().toByteArray()));
		}
		JSONObject json = new JSONObject();
		json.put(KEY_ARRAY, array.toJSONString());
		json.put(KEY_PUBLICKEY, privateSet[0].getPublicKey().toJSON().toJSONString());
		return json;
	}
	
	public static EncryptedInteger[] PrivateSetfromJSON(JSONObject json) throws BigIntegerClassNotValid {
		PublicKey pk = PublicKey.fromJSON((JSONObject) json.get(KEY_PUBLICKEY));
		JSONArray array = (JSONArray) json.get(KEY_ARRAY);
		EncryptedInteger[] r = new EncryptedInteger[array.size()];
		for (int i = 0; i < r.length; i++) {
			r[i] = new EncryptedInteger(new BigInteger(Base64.decodeBase64((String) array.get(i))), pk, true);
		}
		return r;
	}*/
}
