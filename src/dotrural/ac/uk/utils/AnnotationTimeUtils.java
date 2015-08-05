package dotrural.ac.uk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class AnnotationTimeUtils {

	/**
	 * 	 * To be called if annotation.body.type == http://proton.semanticweb.org/2006/05/protont#TimeInterval

	 * @param tweet - the text of the original message
	 * @param annotationValue - the vody of the annotatio 
	 * @param dateStartIndex - the index of tweet that annotationValue starts at
	 * @return
	 */
	public Interval parseTimeInterval(String tweet, String annotationValue,
			int dateStartIndex) {
		String pre = tweet.substring(dateStartIndex - 8, dateStartIndex);
		// the annotation service classifies "Service18 - 22nd May" or "Service 18 - 22nd May" as a time
		// interval when it should be a date
		if ("Service".equalsIgnoreCase(pre.trim())) {
			Calendar date = parseDateString(annotationValue
					.substring(annotationValue.indexOf("-") + 2));
			setToStartOfDay(date);

			Calendar to = Calendar.getInstance();
			to.setTime(date.getTime());
			setToEndOfDay(to);

			return new Interval(date, to);
		} else {
			// check if we actually have a date - date
			int index = annotationValue.indexOf("-");
			if (index > -1) {
				String firstDate = annotationValue.substring(0, index).trim();
				String secondDate = annotationValue.substring(index + 1).trim();
				Calendar secondCal = parseDateString(secondDate);
				Calendar firstCal = parseDateString(firstDate);
				// if string was 18th - 22nd May or 18 - 22 May
				// then firstCal will have failed to parse, bu the second will
				// have
				// given us a date
				if (firstCal == null && secondCal != null) {
					firstDate = removeDatePostfix(firstDate);
					int day = -1;
					try {
						day = Integer.parseInt(firstDate.trim());
					} catch (NumberFormatException nfw) {
						// not a number, do nothing
					}
					if (day != -1) {
						// there was a day
						firstCal = Calendar.getInstance();
						firstCal.set(Calendar.DAY_OF_MONTH, day);
						firstCal.set(Calendar.MONTH,
								secondCal.get(Calendar.MONTH));
						firstCal.set(Calendar.YEAR,
								secondCal.get(Calendar.YEAR));
						setToStartOfDay(firstCal);
					}
					setToEndOfDay(secondCal);
				} else if (firstCal != null && secondCal != null) {
					// date was 12 May - 22 May
					setToStartOfDay(firstCal);
					setToEndOfDay(secondCal);
				} else if (firstCal != null && secondCal == null) {
					// assuming date wouldnt be 12 May - 22 as that would just
					// be
					// strange,
					// so just assume firstCal is the entire value
					secondCal = Calendar.getInstance();
					secondCal.set(Calendar.DAY_OF_MONTH,
							firstCal.get(Calendar.DAY_OF_MONTH));
					secondCal.set(Calendar.MONTH, firstCal.get(Calendar.MONTH));
					secondCal.set(Calendar.YEAR, firstCal.get(Calendar.YEAR));
					setToEndOfDay(secondCal);
				} else {
					// both are null so
					return null;
				}

				return new Interval(firstCal, secondCal);
			}
		}

		// its a format we've not seen before, so don't have a parser for
		return null;
	}

	private void setToStartOfDay(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
	}

	private void setToEndOfDay(Calendar to) {
		to.set(Calendar.HOUR_OF_DAY, 23);
		to.set(Calendar.MINUTE, 59);
		to.set(Calendar.SECOND, 59);
	}

	/**
	 * To be called if annotation.body.type == http://proton.semanticweb.org/2006/05/protonu#CalendarMonth
	 * @param arg1 - The body of the annotation
	 * @return
	 */
	public Interval parseMonth(String arg1) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM");

		// attempt to get the first day of the month
		Calendar d = parse(sdf1, arg1);
		if (d == null) {
			// if no year was provided, set it to this year
			d = parse(sdf2, arg1);
			Calendar now = Calendar.getInstance();
			d.set(Calendar.YEAR, now.get(Calendar.YEAR));
		}

		// now should have something, if not, there is no month here
		if (d == null)
			return null;

		// we have a month, so determine the last day of it
		Calendar to = Calendar.getInstance();
		to.setTime(d.getTime());
		to.set(Calendar.DAY_OF_MONTH, d.getActualMaximum(Calendar.DAY_OF_MONTH));

		return new Interval(d, to);
	}

	private Calendar parseDateString(String arg1) {
		String str4 = removeDatePostfix(arg1);
		Calendar c = Calendar.getInstance();
		int thisYear = c.get(Calendar.YEAR);
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMMM yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("E dd MMMM yyyy");

		Calendar f = parse(sdf1, str4);
		if (f != null)
			return f;

		Calendar h = parse(sdf2, str4);
		if (h != null)
			return h;

		Calendar e = parse(sdf1, str4 + " " + thisYear);
		if (e != null)
			return e;

		Calendar g = parse(sdf2, str4 + " " + thisYear);
		if (g != null)
			return g;
		return null;
	}

	/**
	 * To be called if annotation.body.type == http://proton.semanticweb.org/2006/05/protonu#Date
	 * @param s - The body of the annoation
	 * @return
	 */
	public Interval parseDate(String s) {
		String str = removeDatePostfix(s);
		System.out.println(str);
		Calendar from = parseDateString(str);
		if (from != null) {
		Calendar to = (Calendar) from.clone();
		setToStartOfDay(from);
		setToEndOfDay(to);
		return new Interval(from, to);
		}
		return null;
		}

		private String removeDatePostfix(String arg1) {
		String copy = arg1;
		Pattern stP = Pattern.compile("\\d+st|\\d+nd|\\d+rd|\\d+th");
		Matcher m = stP.matcher(arg1);
		while (m.find()){
		String s = m.group();
		copy = copy.replace(s, s.substring(0, s.length()-2));
		}
		return copy;
		}

	private Calendar parse(SimpleDateFormat sdf, String str) {
		Date date = null;
		try {
			date = sdf.parse(str);
		} catch (ParseException p) {
			date = null;
		}
		if (date != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			return c;
		} else
			return null;
	}

	class Interval {
		private Calendar from, to;

		public Interval(Calendar from, Calendar to) {
			super();
			this.from = from;
			this.to = to;
		}

		public Calendar getFrom() {
			return from;
		}

		public void setFrom(Calendar from) {
			this.from = from;
		}

		public Calendar getTo() {
			return to;
		}

		public void setTo(Calendar to) {
			this.to = to;
		}

	}

	/**
	 * Replaces any rdf:value properties for annotationIndividual with a
	 * owltime:Interval individual with timeline:startsAtDateTime and
	 * endsAtDateTime values
	 * 
	 * @param interval
	 * @param model
	 * @param ns
	 * @param annotationIndividual
	 * @return
	 */
	public Individual addToModel(Interval interval, OntModel model, String ns,
			Individual annotationIndividual) {
		/*
		 * time - http://www.w3.org/2006/time# timeline -
		 * http://purl.org/NET/c4dm/timeline.owl# rdf -
		 * http://www.w3.org/1999/02/22-rdf-syntax-ns# ?timeperiod a
		 * time:Interval . ?timeperiod timeline:beginsAtDateTime ?startDT .
		 * ?timeperiod timeline:endsAtDateTime ?endDT . ?dateAnnotation a
		 * oa:Annotation . ?locationAnnotation2 rdf:value ?label2 .
		 */
		String uri = ns + UUID.randomUUID().toString();
		Individual i = model.createIndividual(uri,
				model.createClass("http://www.w3.org/2006/time#Interval"));
		i.setPropertyValue(
				model.createProperty("http://purl.org/NET/c4dm/timeline.owl#timeline:beginsAtDateTime"),
				model.createTypedLiteral(interval.getFrom()));
		i.setPropertyValue(
				model.createProperty("http://purl.org/NET/c4dm/timeline.owl#timeline:endsAtDateTime"),
				model.createTypedLiteral(interval.getTo()));
		annotationIndividual
				.removeAll(model
						.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"));
		annotationIndividual
				.setPropertyValue(
						model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"),
						i);
		return i;
	}

	public static void main(String[] args) {
		/*AnnotationTimeUtils atu = new AnnotationTimeUtils();
		Interval d = atu
				.parseTimeInterval(
						"George Street diversion - 2st - 26th June - Service 19, 17, 18 .",
						"2nd-26th June", 26);
		// Interval d = atu.parseDate("25 May 2015");
		OntModel m = ModelFactory.createOntologyModel();
		Individual i = m.createIndividual("http://www.example.com/test",
				m.createClass("http://www.example.com/class"));
		atu.addToModel(d, m, "http://www.example.com/data/", i);
		m.write(System.out, "TTL");
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH mm ss");
		System.out.println(sdf.format(d.getFrom().getTime()));
		System.out.println(sdf.format(d.getTo().getTime()));*/
		AnnotationTimeUtils atu = new AnnotationTimeUtils();
		System.out.println(atu.parseDateString("Sunday 17th May 2015"));
	}
}
