package cat.urv.crises.distcom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import thep.paillier.EncryptedInteger;
import thep.paillier.exceptions.BigIntegerClassNotValid;
import thep.paillier.exceptions.PublicKeysNotEqualException;
import cat.urv.crises.distcom.model.Dataset;
import cat.urv.crises.distcom.proto.AckMessage;
import cat.urv.crises.distcom.proto.Constants;
import cat.urv.crises.distcom.proto.ContentMessage;
import cat.urv.crises.distcom.proto.HelloMessage;
import cat.urv.crises.distcom.proto.ResultMessage;

public class Server {
	
	private final Dataset dataset;
	private final ServerSocket ss;
	
	public Server(int port, String datasetPath) throws IOException {
		ss = new ServerSocket(port);
		dataset = Dataset.loadFile(datasetPath);
	}	
	
	public void run() throws IOException  {
		boolean exit = false;
		while (!exit) {
			Log("Waiting for connections...");
			Socket s = ss.accept();
			Log("Connection accepted from " + s.getInetAddress());
			new Thread(new ClientHandler(s)).start();
		}
	}
	
	class ClientHandler implements Runnable {

		private final Socket s;
		private final ObjectInputStream in;
		private final ObjectOutputStream out;
		
		public ClientHandler(Socket s) throws IOException {
			this.s = s;
			try {
				this.in = new ObjectInputStream(s.getInputStream());
				this.out = new ObjectOutputStream(s.getOutputStream());
			} catch (IOException e) {
				Log("Error getting streams from " + s.getInetAddress() + ". Closing connection.");
				throw new IOException(e);
				/* make new exception */
			}
		}
		
		@Override
		public void run() {
			try {
				String session = "Session with " + s.getInetAddress() + ": ";
				
				HelloMessage hello = (HelloMessage) in.readObject();
				String method = hello.method;
				Log(session + "Received Hello Message.");
				out.writeObject(new AckMessage());
				Log(session + "ACK Sent.");
				
				ContentMessage content = (ContentMessage) in.readObject();
				Log(session + "Received Private Set.");
				
				EncryptedInteger[] results = null;
				if (method.equals(Constants.METHOD_CARDINALITY) || method.equals(Constants.METHOD_DIFFERENCE)) {
					results = content.ps.cardinalityIntersection(dataset.encode(), content.crs);
				} else if (method.equals(Constants.METHOD_INTERSECTION)) {
					results = content.ps.intersection(dataset.encode());
				}								
				out.writeObject(new ResultMessage(results));
				Log(session + "Results Sent.");
				Log(session + "Session ended.");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (BigIntegerClassNotValid e) {
				e.printStackTrace();
			} catch (PublicKeysNotEqualException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				in.close();
				out.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		// Parse args
		int port = 8888;
		String dataset = "demo_dataset2.json";
		
		if (args.length == 2) {
			port = Integer.parseInt(args[0]);
			dataset = args[1];
		}
		
		Server s = new Server(port, dataset);
		s.run();
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
}
