package cat.urv.crises.distcom.proto;


public class HelloMessage extends Message {
	
	private static final long serialVersionUID = 1L;
	public final String method;

	public HelloMessage(String method) {
		super(Constants.HELLO_MESSAGE);
		this.method = method;
	}
}
