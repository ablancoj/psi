package cat.urv.crises.distcom.proto;

import thep.paillier.EncryptedInteger;

public class ResultMessage extends Message {

	private static final long serialVersionUID = 1L;
	public final EncryptedInteger[] results;
	
	public ResultMessage(EncryptedInteger[] results) {
		super(Constants.RESULT_MESSAGE);
		this.results = results;
	}

}
