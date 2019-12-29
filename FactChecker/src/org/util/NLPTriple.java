package org.util;

public class NLPTriple {

	private String subject;
	private String predicate;
	private String object;

	public NLPTriple() {
	}

	public NLPTriple(String subject, String predicate, String object) {
		this.subject = subject;
		this.object = object;
		this.predicate = predicate;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

}
