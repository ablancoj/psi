package cat.urv.crises.distcom;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import thep.paillier.EncryptedInteger;
import thep.paillier.exceptions.BigIntegerClassNotValid;
import cat.urv.crises.distcom.model.Dataset;
import cat.urv.crises.distcom.model.KeyPair;
import cat.urv.crises.distcom.model.PrivateSet;
import cat.urv.crises.distcom.proto.Constants;
import cat.urv.crises.distcom.proto.ContentMessage;
import cat.urv.crises.distcom.proto.HelloMessage;
import cat.urv.crises.distcom.proto.ResultMessage;

public class Client {
	
	final static BigInteger crs = BigInteger.valueOf(1010101010);

	private final Socket s;
	private final String address;
	private final int port;
	
	private final Dataset dataset;
	private final String method;
	
	public Client(String address, int port, String path, String method) {
		this.address = address;
		this.port = port;
		this.s = new Socket();
		this.dataset = Dataset.loadFile(path);
		this.method = method;
	}
	
	public Client(String address, int port, Dataset dataset, String method) {
		this.address = address;
		this.port = port;
		this.s = new Socket();
		this.dataset = dataset;
		this.method = method;
	}
	
	public List<String> runIntersection() throws Exception {
		// Prepare keys and payload
		KeyPair kp = new KeyPair(1024);
		BigInteger[] codedSet = dataset.encode();
		PrivateSet privateSet = new PrivateSet(codedSet, kp.getPublicKey());

		// Connect to server
		s.connect(new InetSocketAddress(address, port));
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		
		// Send hello message, indicating the method to execute: intersection or cardinality
		HelloMessage hello = new HelloMessage(this.method);
		out.writeObject(hello);
		in.readObject();
		
		// Send private set and obtain results
		ContentMessage content = null;
		if (method.equals(Constants.METHOD_CARDINALITY) || method.equals(Constants.METHOD_DIFFERENCE)) {
			content = new ContentMessage(privateSet, crs);
		} else if (method.equals(Constants.METHOD_INTERSECTION)) {
			content = new ContentMessage(privateSet, null);
		}
		out.writeObject(content);
		ResultMessage result = (ResultMessage) in.readObject();
		
		// Close connection
		s.close();
		
		// Interpret results
		List<String> ret = new ArrayList<String>();
		for (EncryptedInteger i: result.results) {
			BigInteger j = i.decrypt(kp.getPrivateKey());
			int pos = in(j,dataset.encode());
			if (pos != -1) {
				ret.add(dataset.getValues().get(pos));
			}
		}
		return ret;
	}
	
	public void run() throws BigIntegerClassNotValid, Exception {
		// Prepare keys and payload
		KeyPair kp = new KeyPair(1024);
		BigInteger[] codedSet = dataset.encode();
		PrivateSet privateSet = new PrivateSet(codedSet, kp.getPublicKey());

		// Connect to server
		s.connect(new InetSocketAddress(address, port));
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		
		// Send hello message, indicating the method to execute: intersection or cardinality
		HelloMessage hello = new HelloMessage(this.method);
		out.writeObject(hello);
		in.readObject();
		
		// Send private set and obtain results
		ContentMessage content = null;
		if (method.equals(Constants.METHOD_CARDINALITY) || method.equals(Constants.METHOD_DIFFERENCE)) {
			content = new ContentMessage(privateSet, crs);
		} else if (method.equals(Constants.METHOD_INTERSECTION)) {
			content = new ContentMessage(privateSet, null);
		}
		out.writeObject(content);
		ResultMessage result = (ResultMessage) in.readObject();
		
		// Close connection
		s.close();
		
		// Interpret results
		if (method.equals(Constants.METHOD_CARDINALITY)) {
			int card = 0;
			for (EncryptedInteger i: result.results) {
				if (i.decrypt(kp.getPrivateKey()).equals(crs)) {
					card++;
				}
			}
			System.out.println("Card: "+ card);
		} else if (method.equals(Constants.METHOD_INTERSECTION)) {
			for (EncryptedInteger i: result.results) {
				BigInteger j = i.decrypt(kp.getPrivateKey());
				int pos = in(j,dataset.encode());
				if (pos != -1) {
					System.out.println(dataset.getValues().get(pos));
				}
			}
		} else if (method.equals(Constants.METHOD_DIFFERENCE)) {
			int card = 0;
			for (EncryptedInteger i: result.results) {
				if (i.decrypt(kp.getPrivateKey()).equals(crs)) {
					card++;
				}
			}
			int difference = codedSet.length + result.results.length - (2 * card);
			System.out.println("Difference = " + difference);
		}
	}
	
	public static void Log(String message) {
		Calendar time = Calendar.getInstance();
		int h = time.get(Calendar.HOUR_OF_DAY);
		int m = time.get(Calendar.MINUTE);
		int s = time.get(Calendar.SECOND);
		int ms = time.get(Calendar.MILLISECOND);
		String prefix = "[" + h + ":" + m + ":" + s + "." + ms + "]: ";
		System.out.println(prefix + message);
	}
	
	public static int in(BigInteger a, BigInteger[] set) {
		for (int i = 0; i < set.length; i++) {
			if (set[i].equals(a)) return i;
		}
		return -1;
	}
	
	public static void main(String[] args) throws BigIntegerClassNotValid, Exception {
		Client c = new Client("127.0.0.1", 8888, "function1.json", Constants.METHOD_DIFFERENCE);
		c.run();
	}
}
