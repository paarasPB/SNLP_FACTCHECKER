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
import java.util.stream.Collectors;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Checker {

	private String filePath;
	private Map<String, Map<String, List<Map<String, Double>>>> model;
	private List<Fact> factList;

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
								subject += coreLabel.originalText() + "_";

							}
							// System.out.println("subjects: " + subject.substring(0, subject.length() -
							// 1));

							String object = "";

							for (CoreLabel coreLabel : triple.object) {
								object += coreLabel.originalText() + "_";
							}
							// System.out.println("objects: " + object.substring(0, object.length() - 1));

							String predicate = "";
							for (CoreLabel coreLabel : triple.relation) {
								predicate += coreLabel.originalText() + "_";
							}
							// System.out.println("predicate: " + predicate.substring(0, predicate.length()
							// - 1));

							if (model.containsKey(subject.substring(0, subject.length() - 1))) {

								if (model.get(subject.substring(0, subject.length() - 1))
										.containsKey(predicate.substring(0, predicate.length() - 1))) {

									for (int i = 0; i < model.get(subject.substring(0, subject.length() - 1))
											.get(predicate.substring(0, predicate.length() - 1)).size(); i++) {

										if (model.get(subject.substring(0, subject.length() - 1))
												.get(predicate.substring(0, predicate.length() - 1)).get(i)
												.containsKey(object.substring(0, object.length() - 1))) {

											if (model.get(subject.substring(0, subject.length() - 1))
													.get(predicate.substring(0, predicate.length() - 1)).get(i)
													.get(object.substring(0, object.length() - 1)) == 0.0) {
												fact.setFactValue(-1.0);
											} else {
												fact.setFactValue(model.get(subject.substring(0, subject.length() - 1))
														.get(predicate.substring(0, predicate.length() - 1)).get(i)
														.get(object.substring(0, object.length() - 1)));
											}
											break;
										} else {
											fact.setFactValue(-1.0);
										}

									}

								}
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
						subject += coreLabel.originalText() + "_";

					}
					System.out.println("subjects: " + subject.substring(0, subject.length() - 1));

					String object = "";

					for (CoreLabel coreLabel : triple.object) {
						object += coreLabel.originalText() + "_";
					}
					System.out.println("objects: " + object.substring(0, object.length() - 1));

					String predicate = "";
					for (CoreLabel coreLabel : triple.relation) {
						predicate += coreLabel.originalText() + "_";
					}
					System.out.println("predicate: " + predicate.substring(0, predicate.length() - 1));

					if (model.containsKey(subject.substring(0, subject.length() - 1))) {

						if (model.get(subject.substring(0, subject.length() - 1))
								.containsKey(predicate.substring(0, predicate.length() - 1))) {

							for (int i = 0; i < model.get(subject.substring(0, subject.length() - 1))
									.get(predicate.substring(0, predicate.length() - 1)).size(); i++) {

								if (model.get(subject.substring(0, subject.length() - 1))
										.get(predicate.substring(0, predicate.length() - 1)).get(i)
										.containsKey(object.substring(0, object.length() - 1))) {

									if (model.get(subject.substring(0, subject.length() - 1))
											.get(predicate.substring(0, predicate.length() - 1)).get(i)
											.get(object.substring(0, object.length() - 1)) == 0.0) {
										fact.setFactValue(-1.0);

									} else {
										fact.setFactValue(model.get(subject.substring(0, subject.length() - 1))
												.get(predicate.substring(0, predicate.length() - 1)).get(i)
												.get(object.substring(0, object.length() - 1)));
									}
									break;
								} else {
									fact.setFactValue(-1.0);
								}

							}

						}

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
				bw.write("<http://swc2017.aksw.org/task2/dataset/"+this.factList.get(i).getFactId()+"> <http://swc2017.aksw.org/hasTruthValue> \""
						+this.factList.get(i).getFactValue()+"\"^^<http://www.w3.org/2001/XMLSchema#double> .");
				bw.newLine();
			}

			bw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("file written at "+ filePath);
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
