package cat.urv.crises.distcom.proto;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String messageId;
	
	public Message(String messageId) {
		this.messageId = messageId;
	}

}
