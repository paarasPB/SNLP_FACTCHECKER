package org.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations.RelationTriplesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.*;

public class Parsing {

	public void getTriples() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// read some text in the text variable

		List<String> list = new ArrayList<>();
		List<Fact> factList = new ArrayList<Fact>();
		try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File("SNLP2019_training.tsv")))) {
			list = br.lines().collect(Collectors.toList());

		}

		for (String line : list) {

			if (!line.startsWith("FactID")) {

				Fact fact = new Fact(Integer.valueOf(line.split("\t")[0]), line.split("\t")[1].trim(),
						Double.valueOf(line.split("\t")[2]));
				// Fact fact = new Fact();
				NLPTriple nlpTriple = new NLPTriple();

				// System.out.println(fact.getFactString() + " == " + fact.getFactValue());

				Annotation document = new Annotation(fact.getFactString());
				// Annotation document = new Annotation("Stephen King's birth place is Portland,
				// Maine.");
				// New York City is IBM's innovation place.
				pipeline.annotate(document);

				for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
					// Get the OpenIE triples for the sentence
					Collection<RelationTriple> triples = sentence
							.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
					// Print the triples
					for (RelationTriple triple : triples) {
						System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t"
								+ triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
					}
				}
			}
		}
	}

	public static void getNLPRelationTriples() {

		Document document = new Document("The Aristocats stars Heather Graham");

		for (Sentence sent : document.sentences()) {

			// Iterate over the triples in the sentence
			for (RelationTriple triple : sent.openieTriples()) {
				System.out.println(triple);
				// Print the triple
				// System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() +
				// "\t"
				// + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
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

			}
		}
	}

	public static void main1(String[] args) {
		getNLPRelationTriples();
	}

	public static void main(String[] args) throws IOException {

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER,
		// parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// read some text in the text variable

		List<String> list = new ArrayList<>();
		List<Fact> factList = new ArrayList<Fact>();
		try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File("SNLP2019_training.tsv")))) {
			list = br.lines().collect(Collectors.toList());

		}

		//for (String line : list) {

			//if (!line.startsWith("FactID")) {

		String line="1\tI Will Fear No Evil's author is Iain Banks.\t1.0";
				Fact fact = new Fact(Integer.valueOf(line.split("\t")[0]), line.split("\t")[1].trim(),
						Double.valueOf(line.split("\t")[2]));
				// Fact fact = new Fact();
				NLPTriple nlpTriple = new NLPTriple();

				// System.out.println(fact.getFactString() + " == " + fact.getFactValue());

				Annotation document = new Annotation(fact.getFactString());

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				for (CoreMap sentence : sentences) {

					boolean isSubjectSet = false;

					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

						String word = token.get(TextAnnotation.class);

						String pos = token.get(PartOfSpeechAnnotation.class);

						String ne = token.get(NamedEntityTagAnnotation.class);

						System.out.println(word + " ," + ne + " ," + pos);
						if (!pos.equalsIgnoreCase("NNP") && !pos.equalsIgnoreCase("NN")) {
							isSubjectSet = true;

						}
						if ((pos.equalsIgnoreCase("NNP") || pos.equalsIgnoreCase("NNPS")) && !isSubjectSet) {

							nlpTriple.setSubject(
									nlpTriple.getSubject() == null ? word + "_" : nlpTriple.getSubject() + word + "_");
						}
						if ((pos.equalsIgnoreCase("NNP") || pos.equalsIgnoreCase("NNPS")) && isSubjectSet) {

							nlpTriple.setObject(
									nlpTriple.getObject() == null ? word + "_" : nlpTriple.getObject() + word + "_");
						}

						if (pos.equalsIgnoreCase("NN") || pos.equalsIgnoreCase("VBZ") || pos.equalsIgnoreCase("NNS")) {
							nlpTriple.setPredicate(nlpTriple.getPredicate() == null ? word + "_"
									: nlpTriple.getPredicate() + word + "_");

						}

					}

				}
				if (nlpTriple.getSubject() != null) {
					nlpTriple.setSubject(nlpTriple.getSubject().substring(0, nlpTriple.getSubject().length() - 1));

				}
				if (nlpTriple.getObject() != null) {
					nlpTriple.setObject(nlpTriple.getObject().substring(0, nlpTriple.getObject().length() - 1));
				}
				nlpTriple.setPredicate(nlpTriple.getPredicate().substring(0, nlpTriple.getPredicate().length() - 1));

				// fact.setTriple(nlpTriple); factList.add(fact);

			//}
		//}

		for (int i = 0; i < factList.size(); i++) {
			// System.out.println("subject: " + factList.get(i).getTriple().getSubject() + "
			// predicate: "
			// + factList.get(i).getTriple().getPredicate() + " object: "
			// + factList.get(i).getTriple().getObject());
		}

	}
}
