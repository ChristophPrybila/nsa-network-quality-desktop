package at.ac.tuwien.nsa.datastore;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class ResultStorage implements JSONSerializable {

	private static final Logger LOG = Logger.getLogger(ResultStorage.class);
	private List<QualityMeasurement> results;
	private MeasurementPlace place;

	public ResultStorage(MeasurementPlace place) {
		super();
		this.place = place;
		results = new ArrayList<QualityMeasurement>();
	}

	public void addNewMeasurement(boolean download) {
		synchronized (results) {
			results.add(new QualityMeasurement(download, place));
		}
	}

	public void addDurationToLastIncompleteMeasurement(long duration) {
		synchronized (results) {
			int indexOfLastIncompleteMeasurement = 0;
			for (int index = (results.size() - 1); index >= 0; index--) {
				if (results.get(index).getDuration() != -1) {
					indexOfLastIncompleteMeasurement = index + 1;
					break;
				}
			}
			if (indexOfLastIncompleteMeasurement < results.size()) {
				results.get(indexOfLastIncompleteMeasurement).setDuration(
						duration);
			}
		}
	}

	public void addQualityToLastIncompleteMeasurement(QualityData quality) {
		synchronized (results) {
			int indexOfLastIncompleteMeasurement = 0;
			for (int index = (results.size() - 1); index >= 0; index--) {
				if (results.get(index).getQuality() != null) {
					indexOfLastIncompleteMeasurement = index + 1;
					break;
				}
			}
			if (indexOfLastIncompleteMeasurement < results.size()) {
				results.get(indexOfLastIncompleteMeasurement).setQuality(
						quality);
			}
		}
	}

	public void printStorage() {
		synchronized (results) {
			for (QualityMeasurement measurement : results) {
				LOG.info(measurement.toString());
			}
		}
	}

	@Override
	public String toJSON() {
		StringBuilder sb = new StringBuilder("{\"records\": [");
		for (QualityMeasurement qm : this.results) {
			sb.append(qm.toJSON());
			sb.append(",");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]}");
		
		return sb.toString();
	}
}
