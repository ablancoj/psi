package cat.urv.crises.distcom.proto;

public class AckMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public AckMessage() {
		super(Constants.ACK_MESSAGE);
	}	
}
