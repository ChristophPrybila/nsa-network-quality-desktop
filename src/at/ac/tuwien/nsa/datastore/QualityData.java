package at.ac.tuwien.nsa.datastore;

public class QualityData {

	private int lac;
	private int cellid;
	private int mcc;
	private int mnc;
	private int csq;
	
	public QualityData(int lac, int cellid, int mcc, int mnc, int csq) {
		super();
		this.lac = lac;
		this.cellid = cellid;
		this.mcc = mcc;
		this.mnc = mnc;
		this.csq = csq;
	}

	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	public int getCellid() {
		return cellid;
	}

	public void setCellid(int cellid) {
		this.cellid = cellid;
	}

	public int getMcc() {
		return mcc;
	}

	public void setMcc(int mcc) {
		this.mcc = mcc;
	}

	public int getMnc() {
		return mnc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public int getCsq() {
		return csq;
	}

	public void setCsq(int csq) {
		this.csq = csq;
	}

	@Override
	public String toString() {
		return "QualityData [lac=" + lac + ", cellid=" + cellid + ", mcc="
				+ mcc + ", mnc=" + mnc + ", csq=" + csq + "]";
	}
	
}
