package edu.illinois.cs.cogcomp.illinoisRE.data.ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.common.IOManager;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.illinoisRE.data.CleanDoc;

public class ICDataHandler {

	public static String getDocId(String line) {
		String id = null;
		if (line.indexOf("DOC id=\"") != -1) {
			int i1 = line.indexOf("\"", line.indexOf("DOC id=\""));
			int i2 = line.indexOf("\"", i1);
			id = line.substring(i1, i2);
		}
		return id;
	}

	private static ArrayList<Integer> getInitialSentOffsets(ArrayList<String> lines) {
		ArrayList<Integer> offsets = new ArrayList<Integer>();
		int c = 0;
		for (int i = 0; i < lines.size(); i++) {
			offsets.add(new Integer(c));
			c += lines.get(i).length() + 1;
		}
		return offsets;
	}
	
	private static String extractSourceTextWithMentionTags(ArrayList<String> lines) {
		ArrayList<String> newLines = new ArrayList<String>();
		StringBuffer s;

		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).trim().compareTo("<HEADLINE>") == 0) {
				i += 1;
				s = new StringBuffer();
				while (lines.get(i).trim().compareTo("</HEADLINE>") != 0) {
					s.append(" ");
					s.append(lines.get(i).trim());
					i += 1;
				}
				String myS = s.toString();
				myS = myS.replaceAll("\t", " ");
				myS = myS.replaceAll(" +", " ");
				myS = myS.trim();
				if (myS.length() > 0) {
					newLines.add(myS);
				}
			}
			if (lines.get(i).trim().compareTo("<P>") == 0) {
				i += 1;
				s = new StringBuffer();
				while (lines.get(i).trim().compareTo("</P>") != 0) {
					s.append(" ");
					s.append(lines.get(i).trim());
					i += 1;
				}
				String myS = s.toString();
				myS = myS.replaceAll("\t", " ");
				myS = myS.replaceAll(" +", " ");
				myS = myS.trim();
				if (myS.length() > 0) {
					newLines.add(myS);
				}
			}
		}

		s = new StringBuffer();
		for(int i=0; i<newLines.size(); i++) {
			s.append(newLines.get(i));
			s.append(" ");
		}
		String newString = s.toString();
		newString = newString.replaceAll("\t", " ");
		newString = newString.replaceAll(" +", " ");
		newString = newString.trim();
		
		return newString;
	}

	// ****
	// In ACE, the contents within the xml tags are excluded towards the
	// character offsets
	// In IC, it is easier. Every character, including the contents of the xml
	// tags are counted towards the character offsets.
	@SuppressWarnings("unchecked")
	public static void addAnnotationsToFile(String filename, CleanDoc doc) {
		ArrayList<String> lines = IOManager.readLinesWithoutTrimming(filename);
		doc.setRawSource(lines);

		// just count the length of each line
		ArrayList<Integer> offsets = getInitialSentOffsets(lines);

		HashMap<Integer, StringBuilder> mentionAnnotations = new HashMap<Integer, StringBuilder>();
		ArrayList<String> annotatedLines = new ArrayList<String>();

		// go through the mentions in the doc and initialize the mentionAnnotations
		HashMap<Pair<Integer, Integer>, Mention> mentions = doc.getMentions();

		// I assume for a pair of char offsets < Integer , Integer > , there's only at most 1 mention
		for (Iterator it = mentions.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Pair<Integer, Integer> offset = (Pair<Integer, Integer>) entry.getKey();
			Mention m = (Mention) entry.getValue();
			if (!mentionAnnotations.containsKey(offset.getFirst())) {
				StringBuilder s = new StringBuilder();
				s.append("<e_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(offset.getFirst(), s);
			} else {
				mentionAnnotations.get(offset.getFirst()).append(" <e_id=\"" + m.getId() + "\">");
			}
			if (!mentionAnnotations.containsKey(offset.getSecond())) {
				StringBuilder s = new StringBuilder();
				s.append("</e_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(offset.getSecond(), s);
			} else {
				mentionAnnotations.get(offset.getSecond()).append(" </e_id=\"" + m.getId() + "\">");
			}
		}

		int i1, i2;
		for (int i = 0; i < lines.size(); i++) {
			// if(lines.get(i).indexOf("DOC id=\"")!=-1) {
			// doc.setId(getDocId(lines.get(i)));
			// }
			i1 = offsets.get(i).intValue();
			i2 = i1 + lines.get(i).length() - 1;
			// identify the mention annotations that fall within i1,i2 (i.e. the
			// current sentence)
			TreeMap<Integer, StringBuilder> sentAnnot = new TreeMap<Integer, StringBuilder>();
			for (Iterator it = mentionAnnotations.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int index = ((Integer) entry.getKey()).intValue();
				if ((i1 <= index) && (index <= i2)) {
					// minus by current sentence 's starting offset
					sentAnnot.put(new Integer(((Integer) entry.getKey()).intValue() - i1), (StringBuilder) entry.getValue());
				}
			}
			StringBuilder s = new StringBuilder();
			int priorIndex = 0;
			for (Iterator it = sentAnnot.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int index = ((Integer) entry.getKey()).intValue();

				// first, let's add the substring from priorIndex (inclusive)
				// till index (not inclusive)
				if ((index - priorIndex) > 0) {
					s.append(lines.get(i).substring(priorIndex, index));
				}

				String[] tags = ((StringBuilder) entry.getValue()).toString().split(" ");

				// divide into starting and ending tags
				StringBuilder startTags = new StringBuilder();
				StringBuilder endTags = new StringBuilder();
				for (int j = 0; j < tags.length; j++) {
					if (tags[j].startsWith("<e_")) {
						startTags.append(tags[j]);
					} else if (tags[j].startsWith("</e_")) {
						endTags.append(tags[j]);
					}
				}
				if (startTags.toString().length() > 0) {
					s.append(startTags.toString());
				}
				s.append(lines.get(i).substring(index, index + 1));
				if (endTags.toString().length() > 0) {
					s.append(endTags.toString());
				}
				priorIndex = index + 1;
			}
			s.append(lines.get(i).substring(priorIndex));

			// if(s.toString().length()>0) {
			// System.out.println("**["+s.toString()+"]");
			annotatedLines.add(s.toString());
			// }
		}

		doc.setRawSourceWithMentionTags(annotatedLines);
		doc.setSourceTextWithMentionTags(extractSourceTextWithMentionTags(annotatedLines));
	}

	private static Pair<Integer, Integer> readBeginEndOffsets(String[] tokens) {
		Pair<Integer, Integer> p = null;
		int i1, i2;
		Integer offset1 = null, offset2 = null;

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].startsWith("beg=\"")) {
				i1 = tokens[i].indexOf("\"") + 1;
				i2 = tokens[i].indexOf("\"", i1);
				offset1 = new Integer(tokens[i].substring(i1, i2));
			}
			if (tokens[i].startsWith("end=\"")) {
				i1 = tokens[i].indexOf("\"") + 1;
				i2 = tokens[i].indexOf("\"", i1);
				offset2 = new Integer(tokens[i].substring(i1, i2));
			}
		}
		p = new Pair<Integer, Integer>(offset1, offset2);

		return p;
	}

	

	

	

	
	public static void readMentionRelationAnnotationInfo(String filename, CleanDoc doc) {
		ArrayList<String> lines = IOManager.readLines(filename);
		String line, coarseLabel, tag;
		ArrayList<String> myLines = null, tempLines = null;
		int relationId = 1, mentionId = 1;

		// this will contain only mentions which are grounded in the text
		HashMap<Pair<Integer, Integer>, Mention> mentions = new HashMap<Pair<Integer, Integer>, Mention>();
		List<SemanticRelation> relations = new ArrayList<SemanticRelation>();

		//lines = Util.trimAllSpaces(lines);

		for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
			line = lines.get(lineIndex);
			if (line.indexOf("Relation id=") != -1) {
				coarseLabel = line.substring(line.indexOf("<") + 1, line.indexOf("Relation id="));
				// In IC, this can be: Attack, Affiliation, PersonBio, Fam
				lineIndex += 1;
				myLines = new ArrayList<String>();
				while (lines.get(lineIndex).trim().compareTo("</" + coarseLabel + "Relation>") != 0) {
					myLines.add(lines.get(lineIndex));
					lineIndex += 1;
				}

				if (coarseLabel.compareTo("Affiliation") == 0 || coarseLabel.compareTo("Fam") == 0 || coarseLabel.compareTo("PersonBio") == 0) {
					SemanticRelation r = new SemanticRelation(new Integer(relationId++).toString());
					r.setCoarseLabel(coarseLabel);
					for (int i = 0; i < myLines.size(); i++) {
						if (myLines.get(i).indexOf("<" + coarseLabel + "RelationType id=") != -1) {
							// e.g. : < AffiliationRelationType id ="node-167">
							i += 1;
							tempLines = new ArrayList<String>();
							while (myLines.get(i).trim().compareTo("</" + coarseLabel + "RelationType>") != 0) {
								tempLines.add(myLines.get(i));
								i += 1;
							}
							readRelationAnnotation(tempLines, r);	// get the fine grained relation label
						}

						if (myLines.get(i).startsWith("<Arg1 id=")
								|| myLines.get(i).startsWith("<Arg2 id=")
								|| myLines.get(i).startsWith("<Person1 id=")
								|| myLines.get(i).startsWith("<Person2 id=")
								|| myLines.get(i).startsWith("<Person id=")
								|| myLines.get(i).startsWith("<Location id=")
								|| myLines.get(i).startsWith("<Age id=")
								|| myLines.get(i).startsWith("<Date id=")) {

							tag = myLines.get(i).substring(myLines.get(i).indexOf("<") + 1, myLines.get(i).indexOf(" id="));

							i += 1;
							tempLines = new ArrayList<String>();
							while (myLines.get(i).trim().compareTo("</" + tag + ">") != 0) {
								tempLines.add(myLines.get(i));
								i += 1;
							}

							Mention m = new Mention(new Integer(mentionId).toString() + "-" + new Integer(mentionId).toString());
							mentionId += 1;
							readMentionAnnotation(tempLines, m);	// read begin-offset, end-offset, and semantic-class of the mention
							Pair<Integer, Integer> offset = new Pair<Integer, Integer>(
									new Integer(m.getStartCharOffset()), new Integer(m.getEndCharOffset()));

							// ensure that this mention is grounded in the text,
							// before I use it
							if (offset.getFirst().intValue() != -1 && offset.getSecond().intValue() != -1) {
								// PersonBio
								if (r.getFineLabel().compareTo("attended") == 0) {
									if (tag.compareTo("Person") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Location") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
									// I'm not capturing Date
								} else if (r.getFineLabel().compareTo("citizenOf") == 0) {
									if (tag.compareTo("Person") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Location") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
								} else if (r.getFineLabel().compareTo("hasAge") == 0) {
									if (tag.compareTo("Person") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Age") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
									// I'm not capturing Date
								} else if (r.getFineLabel().compareTo("wasBorn") == 0) {
									if (tag.compareTo("Person") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Location") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
									// I'm not capturing Date, Age
								}
								// Affiliation
								else if (r.getFineLabel().compareTo("hasEmployee") == 0
										|| r.getFineLabel().compareTo("hasLeader") == 0
										|| r.getFineLabel().compareTo("hasMember") == 0
										|| r.getFineLabel().compareTo("hasSubOrg") == 0) {
									if (tag.compareTo("Arg1") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Arg2") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
								}
								// Fam
								else if (r.getFineLabel().compareTo("hasChild") == 0
										|| r.getFineLabel().compareTo("hasSibling") == 0
										|| r.getFineLabel().compareTo("hasSpouse") == 0) {
									if (tag.compareTo("Person1") == 0) {
										setRelationMention(mentions, offset, m, r, "m1");
									} else if (tag.compareTo("Person2") == 0) {
										setRelationMention(mentions, offset, m, r, "m2");
									}
								}
							}
						}
					}

					// In the IC data annotation.. sometimes only contain 1
					// argument, not 2
					if (r.getM1() != null && r.getM2() != null) {
						relations.add(r);
					}
				}
			}
		}

		doc.setRelations(relations);
		doc.setMentions(mentions);
	}

	// just get the fine-grained relation label
	private static void readRelationAnnotation(ArrayList<String> tempLines, SemanticRelation r) {
		String line = null;
		for (int i = 0; i < tempLines.size(); i++) {
			line = tempLines.get(i).trim();
			if (line.startsWith("<TYPE id=\"")) {
				r.setFineLabel(line.substring(line.indexOf(">") + 1, line.indexOf("</TYPE>")));
			}
		}
	}
	
	private static void readMentionAnnotation(ArrayList<String> tempLines, Mention m) {
		String line = null;
		for (int i = 0; i < tempLines.size(); i++) {
			line = tempLines.get(i);
			if (line.startsWith("<Res id=")) {
				while (tempLines.get(i).compareTo("</Res>") != 0) {
					i += 1;
				}
			}
			if (line.startsWith("<Name id=")) {
				while (tempLines.get(i).compareTo("</Name>") != 0) {
					i += 1;
				}
			}
			if (line.startsWith("<AttributedSource id=")) {
				while (tempLines.get(i).compareTo("</AttributedSource>") != 0) {
					i += 1;
				}
			}

			if (line.startsWith("<text id=") && line.indexOf("type=\"manual\"") != -1) {
				// m.setRawText(line.substring(line.indexOf(">")+1,
				// line.indexOf("</text>")));
				String[] tokens = line.split(" ");
				Pair<Integer, Integer> offsets = readBeginEndOffsets(tokens);
				m.setStartCharOffset(offsets.getFirst().intValue());
				m.setEndCharOffset(offsets.getSecond().intValue());
			}
			if (line.startsWith("<TYPE id=\"")) {
				m.setSC(line.substring(line.indexOf(">") + 1, line.indexOf("</TYPE>")));
			}
		}
	}
	
	private static void setRelationMention(HashMap<Pair<Integer, Integer>, Mention> mentions, Pair<Integer, Integer> offset, Mention m, SemanticRelation r, String mentionOrder) {
		if (mentions.containsKey(offset)) {
			if (mentions.get(offset).getSC().compareTo(m.getSC()) != 0) { 
				// shouldn't happen; this means this mention is annotate more than once, and the semantic class given is different
				System.out.println("ERROR: mentions has different SC");
				System.out.println("beg=" + offset.getFirst().intValue() + " end=" + offset.getSecond().intValue());
			} else {
				if (mentionOrder.compareTo("m1") == 0) {
					r.setM1(mentions.get(offset));
				} else if (mentionOrder.compareTo("m2") == 0) {
					r.setM2(mentions.get(offset));
				}
			}
		} else {
			if (mentionOrder.compareTo("m1") == 0) {
				r.setM1(m);
			} else if (mentionOrder.compareTo("m2") == 0) {
				r.setM2(m);
			}
			mentions.put(offset, m);
		}
	}
	
}
