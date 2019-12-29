package org.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.PropertiesUtils;

public class UnSupervisedChecker {

	private String filePath;
	private Map<String, Map<String, List<Map<String, Double>>>> model;
	private List<Fact> factList;

	public static void main(String[] args) {
		UnSupervisedChecker checker = new UnSupervisedChecker();
		// System.out.println(checker.checkFact("1\tApplied Minds' foundation place is
		// Stanford University.").getFactValue());

		checker.checkFacts("SNLP2019_test.tsv");
		checker.writeResults("result.ttl");
	}

	public List<Fact> checkFacts(String filePath) {
		this.filePath = filePath;
		List<String> list = new ArrayList<>();
		factList = new ArrayList<Fact>();
		try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File(this.filePath)))) {
			list = br.lines().collect(Collectors.toList());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : list) {

			if (!line.startsWith("FactID")) {

				Fact fact = new Fact(Integer.valueOf(line.split("\t")[0]), line.split("\t")[1].trim(), 0.0);
				// System.out.println(fact.getFactString() + " == " + fact.getFactValue());

				factList.add(fact);
				for (Sentence sent : new Document(fact.getFactString()).sentences()) {

					int c = 0;
					for (RelationTriple triple : sent.openieTriples()) {

						if (c == 0) {

							// System.out.println(triple + " == " + fact.getFactValue());

							String subject = "";
							for (CoreLabel coreLabel : triple.subject) {
								subject += coreLabel.originalText() + " ";

							}

							String object = "";

							for (CoreLabel coreLabel : triple.object) {
								object += coreLabel.originalText() + " ";
							}

							String predicate = "";
							for (CoreLabel coreLabel : triple.relation) {
								predicate += coreLabel.originalText() + " ";
							}

							try {

								if (fact.getFactString().contains("author")) {

									String parts[] = fact.getFactString().split(" ");
									if (parts[parts.length - 1].equalsIgnoreCase("author.")) {

										object = fact.getFactString().split("is")[0].trim();
										subject = fact.getFactString().split("is")[1].split("'")[0].trim();

									} else {
										object = fact.getFactString().split("is")[1].trim();
										subject = fact.getFactString().split("is")[0].split("'")[0].trim();
									}
									predicate = "author";
								}

								else {
									if (subject.contains("'")) {
										String[] parts = subject.split("'");
										subject = parts[0].trim();
										predicate = parts[1].replaceAll("s ", "").trim();
									} else if (object.contains("'")) {
										String temp_subject = subject;

										String[] parts = object.split("'");
										subject = parts[0].trim();
										predicate = parts[1].replaceAll("s ", "").trim();
										object = temp_subject;
									}

								}

								if (WebCrawler.scraping(subject.trim(), predicate.trim(), object.trim())) {
									fact.setFactValue(1.0);
									// break;
								} else {
									fact.setFactValue(-1.0);
								}
								System.out.println(subject + " ; predicate; " + predicate + "  ;object ;" + object);
							} catch (IOException e) {
								System.err.println(subject + " not found");
								fact.setFactValue(0.0);
							}
							c++;
						}
					}
				}

			}
		}
		return factList;

	}

	public Fact checkFact(String factString) {

		Fact fact = new Fact(Integer.valueOf(factString.split("\t")[0]), factString.split("\t")[1].trim(), 0.0);
		// System.out.println(fact.getFactString() + " == " + fact.getFactValue());

		for (Sentence sent : new Document(fact.getFactString()).sentences()) {

			int c = 0;
			for (RelationTriple triple : sent.openieTriples()) {

				if (c == 0) {

					System.out.println(triple + " == " + fact.getFactValue());

					String subject = "";
					for (CoreLabel coreLabel : triple.subject) {
						subject += coreLabel.originalText() + " ";

					}

					String object = "";

					for (CoreLabel coreLabel : triple.object) {
						object += coreLabel.originalText() + " ";
					}

					String predicate = "";
					for (CoreLabel coreLabel : triple.relation) {
						predicate += coreLabel.originalText() + " ";
					}

					try {

						System.out.println("subjects: " + subject);
						System.out.println("predicate: " + predicate);
						System.out.println("objects: " + object);

						if (fact.getFactString().contains("author")) {

							String parts[] = fact.getFactString().split(" ");
							if (parts[parts.length - 1].equalsIgnoreCase("author.")) {

								object = fact.getFactString().split("is")[0].trim();
								subject = fact.getFactString().split("is")[1].split("'")[0].trim();

							} else {
								object = fact.getFactString().split("is")[1].trim();
								subject = fact.getFactString().split("is")[0].split("'")[0].trim();
							}
							predicate = "author";
						}

						else {
							if (subject.contains("'")) {
								String[] parts = subject.split("'");
								subject = parts[0].trim();
								predicate = parts[1].trim();
							} else if (object.contains("'")) {
								String temp_subject = subject;

								String[] parts = object.split("'");
								subject = parts[0].trim();
								predicate = parts[1].trim();
								object = temp_subject;
							}

						}

						if (WebCrawler.scraping(subject, predicate, object)) {
							fact.setFactValue(1.0);
							// break;
						} else {
							fact.setFactValue(-1.0);
						}
						System.out.println(subject + " ; predicate; " + predicate + "  ;object ;" + object);
					} catch (IOException e) {
						System.err.println(subject + " not found");
						fact.setFactValue(0.0);
					}
					c++;
				}
			}
		}
		return fact;

	}

	public void writeResults(String filePath) {
		try {

			if (this.factList == null) {
				return;
			}

			File fout = new File(filePath);
			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (int i = 0; i < this.factList.size(); i++) {
				bw.write("<http://swc2017.aksw.org/task2/dataset/" + this.factList.get(i).getFactId()
						+ "> <http://swc2017.aksw.org/hasTruthValue> \"" + this.factList.get(i).getFactValue()
						+ "\"^^<http://www.w3.org/2001/XMLSchema#double> .");
				bw.newLine();
			}

			bw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("file written at " + filePath);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<String, Map<String, List<Map<String, Double>>>> getModel() {
		return model;
	}

	public void setModel(Map<String, Map<String, List<Map<String, Double>>>> model) {
		this.model = model;
	}

}
