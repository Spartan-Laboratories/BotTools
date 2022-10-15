package BotTools.dataprocessing;

import com.spartanlaboratories.util.Date;

public class DateTimeParser {
	public Date readDateMMDD(String rawDate) {
		String month = rawDate.substring(0, 2);
		String date = rawDate.substring(3,5);
		return readDate(date, month);
	}
	public Date readDateDDMM(String rawDate){
		String month = rawDate.substring(3,5);
		String date = rawDate.substring(0,2);
		return readDate(date,month);
	}
	private Date readDate(String date, String month) {
		try {
			return new Date(Integer.parseInt(date), Integer.parseInt(month));
		}catch(NumberFormatException e) {
			throw new IllegalArgumentException("Date needs to be in format: mm/dd");
		}
	}
}
