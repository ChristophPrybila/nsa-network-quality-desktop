package at.ac.tuwien.nsa.datastore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class QualityMeasurement implements JSONSerializable {

	private long duration;
	private boolean download;
	private MeasurementPlace place;
	private Calendar creationDate;
	private QualityData quality;

	public QualityMeasurement(boolean download, MeasurementPlace place) {
		super();
		this.download = download;
		this.place = place;
		this.creationDate = Calendar.getInstance();
		this.duration = -1;
		this.quality = null;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public MeasurementPlace getPlace() {
		return place;
	}

	public void setPlace(MeasurementPlace place) {
		this.place = place;
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	public QualityData getQuality() {
		return quality;
	}

	public void setQuality(QualityData quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
		return "QualityMeasurement [duration=" + duration + ", download=" + download + ", place="
			+ place + ", creationDate=" + sdf.format(creationDate.getTime()) + ", quality=" + quality
			+ "]";
	}

	@Override
	public String toJSON() {
		return String
			.format(
				"{\"time\": %d, \"place\": { \"lat\": %s, \"lng\": %s }, \"signal\": { \"mcc\": %d, \"mnc\": %d, \"lac\": %d, \"cellid\": %d, \"csq\": %d }, \"measurement\": { \"type\": \"%s\", \"duration\": %d } }",
				this.getCreationDate().getTime().getTime()/1000,
				String.valueOf(this.getPlace().getLat()).replace(',', '.'),
				String.valueOf(this.getPlace().getLng()).replace(',', '.'),
				this.getQuality().getMcc(),
				this.getQuality().getMnc(),
				this.getQuality().getLac(),
				this.getQuality().getCellid(),
				this.getQuality().getCsq(),
				this.isDownload() ? "download" : "upload",
				this.getDuration());
	}

}
