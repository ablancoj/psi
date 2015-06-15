package cat.urv.crises.distcom.proto;

import java.math.BigInteger;

import cat.urv.crises.distcom.model.PrivateSet;

public class ContentMessage extends Message {
	
	private static final long serialVersionUID = 1L;
	public final PrivateSet ps;
	public final BigInteger crs;

	public ContentMessage(PrivateSet ps, BigInteger crs) {
		super(Constants.CONTENT_MESSAGE);
		this.ps = ps;
		this.crs = crs;
	}


}
