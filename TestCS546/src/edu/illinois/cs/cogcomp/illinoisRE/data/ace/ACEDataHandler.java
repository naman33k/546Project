/**
 * 
 */
package edu.illinois.cs.cogcomp.illinoisRE.data.ace;

import java.io.BufferedReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.data.curator.TokenAligner;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;
import edu.illinois.cs.cogcomp.illinoisRE.common.IOManager;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.illinoisRE.data.CleanDoc;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;

/**
 * @author dxquang Aug 4, 2010
 */
public class ACEDataHandler {

	public static final String TEXT_ANNOTATION = "text";
	public static final String START_ANNOTATION = "start";
	public static final String END_ANNOTATION = "end";
	
	// CHECKED
    private static String stripXmlTags(String line) {
    	String cleanLine = line;
    	for (String tag : Constants.setXMLTags) {
    		cleanLine = cleanLine.replaceAll("<" + tag + ".*?>", "");
    		cleanLine = cleanLine.replaceAll("</" + tag + ">", "");
    	}
    	return cleanLine;
    }
    
    // CHECKED
	// do not count xml tags towards char offsets
    private static ArrayList<Integer> getInitialSentOffsets(ArrayList<String> lines) {
    	ArrayList<Integer> offsets = new ArrayList<Integer>();
    	int c = 0;
    	String s = new String("");
    	for(int i=0; i<lines.size(); i++) {
    		offsets.add(new Integer(c));
    		s = stripXmlTags(lines.get(i));
    		c += s.length()+1;
    	}
    	return offsets;
    }

    
    // CHECKED
    private static String extractSourceTextWithMentionTags(ArrayList<String> lines, String corpusId) {
		StringBuffer s = new StringBuffer();
		int i;
		System.out.println(corpusId);
		if(corpusId.compareTo("ace2005nw")==0) {
			for(i=0; lines.get(i).indexOf("<TEXT>")==-1; i++) {}
			i += 1;
			while(lines.get(i).indexOf("</TEXT>")==-1) {
				s.append(" ");
				s.append(stripXmlTags(lines.get(i)));
				i += 1;
			}
		}
		else if(corpusId.compareTo("ace2005bc")==0 || corpusId.compareTo("ace2005bn")==0 || corpusId.compareTo("ace2005cts")==0) {
			for(i=0; lines.get(i).indexOf("<TEXT>")==-1; i++) {}
			i += 1;
			while(lines.get(i).indexOf("</TEXT>")==-1) {
				if(lines.get(i).indexOf("<TURN>")!=-1) {
					i += 1;
					while(lines.get(i).indexOf("</TURN>")==-1) {
						if(lines.get(i).indexOf("<SPEAKER>")==-1 && lines.get(i).indexOf("</SPEAKER>")==-1) {
							s.append(" ");
							s.append(stripXmlTags(lines.get(i)));
						}
						i += 1;
					}
				}
				i += 1;
			}
		}
		else if(corpusId.compareTo("ace2005wl")==0) {
			for(i=0; lines.get(i).indexOf("<TEXT>")==-1; i++) {}
			i += 1;
			while(lines.get(i).indexOf("</TEXT>")==-1) {
				if( lines.get(i).indexOf("<POST>")==-1 && lines.get(i).indexOf("</POST>")==-1 &&
					lines.get(i).indexOf("<POSTER>")==-1 && lines.get(i).indexOf("</POSTER>")==-1 &&
					lines.get(i).indexOf("<POSTDATE>")==-1 && lines.get(i).indexOf("</POSTDATE>")==-1) {
					s.append(" ");
					s.append(stripXmlTags(lines.get(i)));
				}
				i += 1;
			}
		}
		else if(corpusId.compareTo("ace2004nwire")==0 || corpusId.compareTo("ace2004atb")==0 || corpusId.compareTo("ace2004ctb")==0) {
			for(i=0; lines.get(i).indexOf("<TEXT>")==-1; i++) {}
			i += 1;
			while(lines.get(i).indexOf("</TEXT>")==-1) {
				s.append(" ");
				s.append(stripXmlTags(lines.get(i)));
				i += 1;
			}
		}
		else if(corpusId.compareTo("ace2004bnews")==0) {
			for(i=0; lines.get(i).indexOf("<TEXT>")==-1; i++) {}
			i += 1;
			while(lines.get(i).indexOf("</TEXT>")==-1) {
				if( lines.get(i).indexOf("<TURN>")==-1 && lines.get(i).indexOf("</TURN>")==-1 &&
					lines.get(i).indexOf("<ANNOTATION>")==-1 && lines.get(i).indexOf("</ANNOTATION>")==-1) {	
					s.append(" ");
					s.append(stripXmlTags(lines.get(i)));
				}
				i += 1;
			}
		}
		
		String myS = s.toString();
		myS = myS.replaceAll("\t", " ");
		myS = myS.replaceAll(" +", " ");
		//myString = myString.replaceAll("\\s+", " ");	// replace multiple spaces with single space
		return myS.trim();
	}

    // CHECKED
    public static void addAnnotationsToFile(String filename, CleanDoc doc) {
    	ArrayList<String> lines = IOManager.readLinesWithoutTrimming(filename);
		doc.setRawSource(lines);

		// just count the length of each line
		ArrayList<Integer> offsets = getInitialSentOffsets(lines);	// for IC, I wouldn't strip xml tags

		HashMap<Integer, StringBuffer> mentionAnnotations = new HashMap<Integer, StringBuffer>();
		ArrayList<String> annotatedLines = new ArrayList<String>();

		// go through the mentions in the doc and initialize the mentionAnnotations
		HashMap<Pair<Integer, Integer>, Mention> mentions = doc.getMentions();

		// I assume for a pair of char offsets < Integer , Integer > , there's only at most 1 mention
		for (Iterator it = mentions.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Pair<Integer, Integer> offset = (Pair<Integer, Integer>) entry.getKey();
			Mention m = (Mention) entry.getValue();
			//System.out.println("ACEDataHandler.addAnnotationsToFile "+m);
			
			if (!mentionAnnotations.containsKey(m.getStartCharOffset())) {
				StringBuffer s = new StringBuffer();
				s.append("<e_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(m.getStartCharOffset(), s);
			} else {
				mentionAnnotations.get(m.getStartCharOffset()).append(" <e_id=\"" + m.getId() + "\">");
			}
			if (!mentionAnnotations.containsKey(m.getHeadStartCharOffset())) {
				StringBuffer s = new StringBuffer();
				s.append("<h_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(m.getHeadStartCharOffset(), s);
			} else {
				mentionAnnotations.get(m.getHeadStartCharOffset()).append(" <h_id=\"" + m.getId() + "\">");
			}
			
			if (!mentionAnnotations.containsKey(m.getHeadEndCharOffset())) {
				StringBuffer s = new StringBuffer();
				s.append("</h_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(m.getHeadEndCharOffset(), s);
			} else {
				mentionAnnotations.get(m.getHeadEndCharOffset()).append(" </h_id=\"" + m.getId() + "\">");
			}
			if (!mentionAnnotations.containsKey(m.getEndCharOffset())) {
				StringBuffer s = new StringBuffer();
				s.append("</e_id=\"" + m.getId() + "\">");
				mentionAnnotations.put(m.getEndCharOffset(), s);
			} else {
				mentionAnnotations.get(m.getEndCharOffset()).append(" </e_id=\"" + m.getId() + "\">");
			}
			
		}

		int i1, i2;
		for (int i = 0; i < lines.size(); i++) {
			if( lines.get(i).indexOf("<TEXT>")==-1 && lines.get(i).indexOf("</TEXT>")==-1 &&
				lines.get(i).indexOf("<TURN>")==-1 && lines.get(i).indexOf("</TURN>")==-1 &&
				lines.get(i).indexOf("<SPEAKER>")==-1 && lines.get(i).indexOf("</SPEAKER>")==-1 &&
				lines.get(i).indexOf("<POST>")==-1 && lines.get(i).indexOf("</POST>")==-1 &&
				lines.get(i).indexOf("<POSTER>")==-1 && lines.get(i).indexOf("</POSTER>")==-1 &&
				lines.get(i).indexOf("<POSTDATE>")==-1 && lines.get(i).indexOf("</POSTDATE>")==-1 &&
				lines.get(i).indexOf("<ANNOTATION>")==-1 && lines.get(i).indexOf("</ANNOTATION>")==-1) {
				String currentLine = stripXmlTags(lines.get(i));	// for IC, I wouldn't stripXmlTags
				// if(lines.get(i).indexOf("DOC id=\"")!=-1) {
				// doc.setId(getDocId(lines.get(i)));
				// }
				i1 = offsets.get(i).intValue();
				i2 = i1 + currentLine.length() - 1;	
				// identify the mention annotations that fall within i1,i2 (i.e. the current sentence)
				TreeMap<Integer, StringBuffer> sentAnnot = new TreeMap<Integer, StringBuffer>();	// this is just for this current sentence
				for (Iterator it = mentionAnnotations.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					int index = ((Integer) entry.getKey()).intValue();
					if ((i1 <= index) && (index <= i2)) {
						// minus by current sentence's starting offset
						sentAnnot.put(new Integer(((Integer) entry.getKey()).intValue() - i1), (StringBuffer) entry.getValue());
					}
				}
				StringBuffer s = new StringBuffer();
				int priorIndex = 0;
				for (Iterator it = sentAnnot.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					int index = ((Integer) entry.getKey()).intValue();

					// first, let's add the substring from priorIndex (inclusive) till index (not inclusive)
					if ((index - priorIndex) > 0) {
						s.append(currentLine.substring(priorIndex, index));
					}

					String[] tags = ((StringBuffer) entry.getValue()).toString().split(" ");

					// divide into starting and ending tags
					StringBuffer startTags = new StringBuffer();
					StringBuffer endTags = new StringBuffer();
					for (int j = 0; j < tags.length; j++) {
						if (tags[j].startsWith("<e_")) {
							startTags.append(tags[j]);
						}
						else if (tags[j].startsWith("<h_")) {
							startTags.append(tags[j]);
						}
						else if (tags[j].startsWith("</h_")) {
							endTags.append(tags[j]);
						}
						else if (tags[j].startsWith("</e_")) {
							endTags.append(tags[j]);
						}
					}
					if (startTags.toString().length() > 0) {
						s.append(startTags.toString());
					}
					s.append(currentLine.substring(index, index + 1));
					if (endTags.toString().length() > 0) {
						s.append(endTags.toString());
					}
					priorIndex = index + 1;
				}
				s.append(currentLine.substring(priorIndex));

				// if(s.toString().length()>0) {
				// System.out.println("**["+s.toString()+"]");
				annotatedLines.add(s.toString());
				// }
			}
			else {
				annotatedLines.add(lines.get(i));
			}
		}
		
		doc.setRawSourceWithMentionTags(annotatedLines);
		doc.setSourceTextWithMentionTags(extractSourceTextWithMentionTags(annotatedLines, doc.getCorpusId()));
		
		//System.out.println("sourceTextWithMentionTags: "+doc.getSourceTextWithMentionTags());
	}
    
	// CHECKED
	// filename: xml file
	public static void readMentionRelationAnnotationInfo(String filename, CleanDoc doc) {
		ArrayList<String> xmlLines = IOManager.readLines(filename);
		HashMap<Pair<Integer, Integer>, Mention> mentions = new HashMap<Pair<Integer, Integer>, Mention>();
		
		List<Mention> mentionList = readMentionsAnnotationInfo(xmlLines);
		for(int i=0; i<mentionList.size(); i++) {
			Mention m = mentionList.get(i);
			mentions.put(new Pair<Integer, Integer>(m.getStartCharOffset(), m.getEndCharOffset()), m);
		}
		
		List<SemanticRelation> relations = readRelationsAnnotationInfo(xmlLines, mentionList);
		
		doc.setRelations(relations);
		doc.setMentions(mentions);
	}
	
	// CHECKED
	// for each mention, the following will be set:
	// mentionLevel, sc, fineSc, surfaceString, startCharOffset, endCharOffset, headSurfaceString, headStartCharOffset, headEndCharOffset
	private static List<Mention> readMentionsAnnotationInfo(ArrayList<String> arrAnnotatedLines) {
		List<Mention> mentions = new ArrayList<Mention>();

		int n = arrAnnotatedLines.size();

		int i = 0;

		boolean startEntity = false;
		boolean startMention = false;
		boolean startExtent = false;
		boolean startExtentCharseq = false;
		boolean startHead = false;
		boolean startHeadCharseq = false;

		String entityType = "";
		String entitySubType = "";
		String mentionLevel = "";
		String extentCharseq = "";
		String headCharseq = "";
		String mentionId = "";

		Mention mention = null;
		while (i < n) {

			String line = arrAnnotatedLines.get(i);

			if (line.startsWith("<entity ID")) {
				startEntity = true;
				entityType = getFieldValue(line, " TYPE");
				if(line.indexOf(" SUBTYPE")!=-1) {
					entitySubType = entityType + ":" + getFieldValue(line, " SUBTYPE");
					//System.out.println("ACEDataHandler.readMentionsAnnotationInfo entitySubType="+entitySubType);
				}
				else {
					entitySubType = entityType;
				}
			} else if (line.startsWith("</entity>") && startEntity == true) {
				startEntity = false;
			}

			else if (line.startsWith("<entity_mention ") && startEntity == true) {
				startMention = true;
				mentionLevel = getFieldValue(line, " TYPE");
				mentionId = getFieldValue(line, " ID");
				
				mention = new Mention(mentionId);
				mention.setMentionLevel(mentionLevel);
				mention.setSC(entityType);
				mention.setFineSC(entitySubType);
			} else if (line.startsWith("</entity_mention>") && startMention == true) {
				startMention = false;
				mention.setEndCharOffset(mention.getHeadEndCharOffset());	// let me reset the extend-end-offset to the head-end-offset
				mention.reviseSurfaceString();
				mentions.add(mention);
			}

			else if (line.startsWith("<extent>") && startMention == true) {
				startExtent = true;
			} else if (line.startsWith("</extent>") && startExtent == true) {
				startExtent = false;
			}

			else if (line.startsWith("<charseq ") && startExtent == true) {
				startExtentCharseq = true;
			}

			else if (line.startsWith("<head>") && startMention == true) {
				startHead = true;
			} else if (line.startsWith("</head>") && startHead == true) {
				startHead = false;
			}

			else if (line.startsWith("<charseq ") && startHead == true) {
				startHeadCharseq = true;
			}

			if (startExtentCharseq == true) {
				extentCharseq += line + " ";
			}

			if (startHeadCharseq == true) {
				headCharseq += line + " ";
			}

			// this code is great, because in ACE .apf.xml files, <charseq> ... </charseq> can cross multiple lines
			if (line.endsWith("</charseq>") && startExtentCharseq == true) {
				startExtentCharseq = false;
				Map<String, String> charseqInfo = getCharSeqInfo(extentCharseq.trim(), "charseq", "START", "END");
				mention.setSurfaceString(charseqInfo.get(TEXT_ANNOTATION));
				mention.setStartCharOffset(Integer.parseInt(charseqInfo.get(START_ANNOTATION)));
				mention.setEndCharOffset(Integer.parseInt(charseqInfo.get(END_ANNOTATION)));
				extentCharseq = "";
			}

			if (line.endsWith("</charseq>") && startHeadCharseq == true) {
				startHeadCharseq = false;
				Map<String, String> charseqInfo = getCharSeqInfo(headCharseq.trim(), "charseq", "START", "END");
				mention.setHeadSurfaceString(charseqInfo.get(TEXT_ANNOTATION));
				mention.setHeadStartCharOffset(Integer.parseInt(charseqInfo.get(START_ANNOTATION)));
				mention.setHeadEndCharOffset(Integer.parseInt(charseqInfo.get(END_ANNOTATION)));
				headCharseq = "";
			}

			i++;
		}
		
		return mentions;
	}

	// CHECKED
	// for each relation, the following will be set:
	// lexicalCondition, coarseLabel, fineLabel, surfaceString, startCharOffset, endCharOffset, m1, m2
	private static List<SemanticRelation> readRelationsAnnotationInfo(ArrayList<String> arrAnnotatedLines, List<Mention> mentions) {
		List<SemanticRelation> relations = new ArrayList<SemanticRelation>();

		HashMap<String, Mention> mentionMap = new HashMap<String, Mention>();	// so that I can quickly retrieve a mention via its id
		for(int mIndex=0; mIndex<mentions.size(); mIndex++) {
			Mention m = mentions.get(mIndex);
			mentionMap.put(m.getId(), m);
		}
		
		int n = arrAnnotatedLines.size();

		int i = 0;

		boolean startRelation = false;
		boolean startRelationMention = false;
		boolean startExtent = false;
		boolean startExtentCharseq = false;
		boolean startRelationMentionArgument = false;

		String relationType = "";
		String relationSubType = "";
		String extentCharseq = "";
		String relationMentionId = "";
		String lexicalCondition = "";
		String m1Id = "";
		String m2Id = "";
		String argRole = "";
		
		SemanticRelation r = null;
		while (i < n) {

			String line = arrAnnotatedLines.get(i);

			if (line.startsWith("<relation ID")) {
				startRelation = true;
				relationType = getFieldValue(line, " TYPE");
				if(line.indexOf(" SUBTYPE")!=-1) {
					relationSubType = relationType + ":" + getFieldValue(line, " SUBTYPE");
				}
				else {
					relationSubType = relationType;
				}
			} else if (line.startsWith("</relation>") && startRelation == true) {
				startRelation = false;
			}

			else if (line.startsWith("<relation_mention ID") && startRelation == true) {
				startRelationMention = true;
				lexicalCondition = getFieldValue(line, "LEXICALCONDITION");
				relationMentionId = getFieldValue(line, " ID");
				
				r = new SemanticRelation(relationMentionId);
				r.setLexicalCondition(lexicalCondition);
				r.setCoarseLabel(relationType);
				r.setFineLabel(relationSubType);
				
				m1Id = "";
				m2Id = "";
				argRole = "";
			} else if (line.startsWith("</relation_mention>") && startRelationMention == true) {
				startRelationMention = false;
				
				Mention m1 = mentionMap.get(m1Id);
				Mention m2 = mentionMap.get(m2Id);
				
				if(m1!=null && m2!=null) {
					r.setM1(m1);
					r.setM2(m2);
				}
				
				relations.add(r);
			}

			else if (line.startsWith("<extent>") && startRelationMention == true) {
				startExtent = true;
			} else if (line.startsWith("</extent>") && startExtent == true) {
				startExtent = false;
			}
			
			else if (line.startsWith("<ldc_extent>") && startRelationMention == true) {		// ace2004
				startExtent = true;
			} else if (line.startsWith("</ldc_extent>") && startExtent == true) {
				startExtent = false;
			}

			else if (line.startsWith("<charseq ") && startExtent==true && startRelationMentionArgument==false) {
				startExtentCharseq = true;
			}

			else if (line.startsWith("<relation_mention_argument ")) {		// ace2005
				startRelationMentionArgument = true;
				argRole = getFieldValue(line, " ROLE");
				if(argRole.compareTo("Arg-1")==0) {
					m1Id = getFieldValue(line, " REFID");
				}
				else if(argRole.compareTo("Arg-2")==0) {
					m2Id = getFieldValue(line, " REFID");
				}
			}
			else if (line.startsWith("</relation_mention_argument")) {
				startRelationMentionArgument = false;
			}
			
			else if (line.startsWith("<rel_mention_arg ")) {				// ace2004
				startRelationMentionArgument = true;
				argRole = getFieldValue(line, " ARGNUM");
				if(argRole.compareTo("1")==0) {
					m1Id = getFieldValue(line, " ENTITYMENTIONID");
				}
				else if(argRole.compareTo("2")==0) {
					m2Id = getFieldValue(line, " ENTITYMENTIONID");
				}
			}
			else if (line.startsWith("</rel_mention_arg")) {
				startRelationMentionArgument = false;
			}
			
			
			if (startExtentCharseq == true) {
				extentCharseq += line + " ";
			}

			// this code is great, because in ACE .apf.xml files, <charseq> ... </charseq> can cross multiple lines
			if (line.endsWith("</charseq>") && startExtentCharseq == true) {
				startExtentCharseq = false;
				Map<String, String> charseqInfo = getCharSeqInfo(extentCharseq.trim(), "charseq", "START", "END");
				r.setSurfaceString(charseqInfo.get(TEXT_ANNOTATION));
				r.setStartCharOffset(Integer.parseInt(charseqInfo.get(START_ANNOTATION)));
				r.setEndCharOffset(Integer.parseInt(charseqInfo.get(END_ANNOTATION)));
				extentCharseq = "";
			}
	
			i++;
		}

		return relations;
	}

	
	// CHECKED
	private static Map<String, String> getCharSeqInfo(String xmlString, String textKey, String startKey, String endKey) {

		Map<String, String> map = new HashMap<String, String>();

		String text = getXMLValue(xmlString, textKey);
		String start = getFieldValue(xmlString, startKey);
		String end = getFieldValue(xmlString, endKey);

		map.put(TEXT_ANNOTATION, text);
		map.put(START_ANNOTATION, start);
		map.put(END_ANNOTATION, end);

		return map;
	}
	
	// checked
	private static String getXMLValue(String inputString, String key) {

		int pos = inputString.indexOf("<" + key);

		if (pos == -1) {
			System.out.println("ERROR: Unable to find " + key + " in the input string.");
			System.out.println("Input string = " + inputString);
			System.exit(1);
		}

		int pos1 = inputString.indexOf(">", pos + 1);
		int pos2 = inputString.indexOf("</" + key + ">");
		String value = inputString.substring(pos1 + 1, pos2);

		return value.trim();
	}

	// checked
	private static String getFieldValue(String line, String key) {
		int pos = line.indexOf(key);

		if (pos == -1) {
			System.out.println("ERROR: Unable to get value of the key " + key);
			System.out.println("Line = " + line);
			System.exit(1);
		}

		int pos1 = line.indexOf("\"", pos);
		int pos2 = line.indexOf("\"", pos1 + 1);

		if (pos1 == -1 || pos2 == -1) {
			System.out.println("ERROR: Unable to get value of the key " + key);
			System.out.println("Line = " + line);
			System.exit(1);
		}

		String value = line.substring(pos1 + 1, pos2);

		return value;
	}

//	public static void main(String[] args) {
//		ACEDataHandler labeler = new ACEDataHandler();
//		labeler
//				.annotateMultipleFiles("/Users/dxquang/tmp/mentiondetection/masterfile");
//	}

}
