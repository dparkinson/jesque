package net.greghaines.jesque.utils;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CompositeDateFormat extends DateFormat {

	private static final long serialVersionUID = -4079876635509458541L;
	private static final List<DateFormatFactory> DATE_FORMAT_FACTORIES = 
		Arrays.<DateFormatFactory>asList(
			new DateFormatFactory() {
				public DateFormat create() {
					return ResqueDateFormatThreadLocal.getInstance();
				}
			},
			new PatternDateFormatFactory(ResqueConstants.DATE_FORMAT_RUBY_V1),
			new PatternDateFormatFactory(ResqueConstants.DATE_FORMAT_RUBY_V2),
			new PatternDateFormatFactory(ResqueConstants.DATE_FORMAT_RUBY_V3),
			new PatternDateFormatFactory(ResqueConstants.DATE_FORMAT_RUBY_V4),
			new PatternDateFormatFactory(ResqueConstants.DATE_FORMAT_PHP)
		);

	@Override
	public StringBuffer format(final Date date, final StringBuffer toAppendTo,
			final FieldPosition fieldPosition) {
		return ResqueDateFormatThreadLocal.getInstance().format(date, toAppendTo, fieldPosition);
	}

	@Override
	public Date parse(final String dateStr, final ParsePosition pos) {
		final ParsePosition posCopy = new ParsePosition(pos.getIndex());
		Date date = null;
		boolean success = false;
		for (final DateFormatFactory dfFactory : DATE_FORMAT_FACTORIES) {
			posCopy.setIndex(pos.getIndex());
			posCopy.setErrorIndex(pos.getErrorIndex());
			date = dfFactory.create().parse(dateStr, posCopy);
			if (posCopy.getIndex() != 0) {
				success = true;
				break;
			}
		}
		if (success) {
			pos.setIndex(posCopy.getIndex());
			pos.setErrorIndex(posCopy.getErrorIndex());
		}
		return date;
	}
	
	private interface DateFormatFactory {
		DateFormat create();
	}
	
	private static class PatternDateFormatFactory implements DateFormatFactory {
		private final String pattern;
		
		public PatternDateFormatFactory(final String pattern) {
			this.pattern = pattern;
		}

		public DateFormat create() {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(this.pattern);
			dateFormat.setLenient(false);
			return dateFormat;
		}
	}
}