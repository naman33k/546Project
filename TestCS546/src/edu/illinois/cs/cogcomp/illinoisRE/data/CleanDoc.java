package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Util;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class CleanDoc {
	private String id;
	private String corpusId;
	private List<SemanticRelation> relations;
	private HashMap<Pair<Integer, Integer>, Mention> mentions;
	private HashMap<Pair<Integer, Integer>, Mention> cleanMentions;	// these are mentions that are annotated by the ACE/IC annotators
																	// also, the start,end index offsets are into the cleanText (which is untokenized, and without any xml tags)
	public String xmlText;
	private String cleanText;							// source text, with no xml tags whatsoever
	public ArrayList<String> rawSource;				// the raw src texts
	private ArrayList<String> rawSourceWithMentionTags;	// =rawSource, but with mentions start & end tags added
	private String sourceTextWithMentionTags;			// =rawSourceWithMentionTags, but containing only portions within <TEXT>...</TEXT>; Also, multiple line(s) breaks within these tags are also combined into a single line.
	private ArrayList<String> plainLines;
	private ArrayList<ArrayList<Pair<Integer, String>>> lineTags;
	
	public CleanDoc() {
		relations = new ArrayList<SemanticRelation>();
		mentions = new HashMap<Pair<Integer, Integer>, Mention>();
		//sourceTextWithMentionTags = new ArrayList<String>();
		cleanMentions = new HashMap<Pair<Integer, Integer>, Mention>();
	}

	
	
	
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setCorpusId(String id) {
		corpusId = id;
	}

	public String getCorpusId() {
		return corpusId;
	}

	public void setRelations(List<SemanticRelation> relations) {
		this.relations = relations;
	}

	public List<SemanticRelation> getRelations() {
		return relations;
	}

	public void setMentions(HashMap<Pair<Integer, Integer>, Mention> mentions) {
		this.mentions = mentions;
	}

	public HashMap<Pair<Integer, Integer>, Mention> getMentions() {
		return mentions;
	}

	/**
	 * @return the cleanMentions
	 */
	public HashMap<Pair<Integer, Integer>, Mention> getCleanMentions() {
		return cleanMentions;
	}

	public void setRawSource(ArrayList<String> text) {
		rawSource = text;
	}

	public ArrayList<String> getRawSource() {
		return rawSource;
	}

	public void setRawSourceWithMentionTags(ArrayList<String> text) {
		rawSourceWithMentionTags = text;
	}

	public ArrayList<String> getRawSourceWithMentionTags() {
		return rawSourceWithMentionTags;
	}

	public void setSourceTextWithMentionTags(String line) {
		sourceTextWithMentionTags = line;
	}

	public String getSourceTextWithMentionTags() {
		return sourceTextWithMentionTags;
	}

	public void setPlainLines(ArrayList<String> plainLines) {
		this.plainLines = plainLines;
	}

	public ArrayList<String> getPlainLines() {
		return plainLines;
	}

	public void setLineTags(ArrayList<ArrayList<Pair<Integer, String>>> lineTags) {
		this.lineTags = lineTags;
	}

	public ArrayList<ArrayList<Pair<Integer, String>>> getLineTags() {
		return lineTags;
	}

	public void printRawSourceWithMentionTags() {
		for (int i = 0; i < rawSourceWithMentionTags.size(); i++) {
			System.out.println(rawSourceWithMentionTags.get(i));
		}
	}

	public void printSourceTextWithMentionTags() {
		System.out.println(sourceTextWithMentionTags);
	}
	
	public void printRelations() {
		System.out.println("<RELATION>");
		for (int i = 0; i < relations.size(); i++) {
			System.out.println(relations.get(i));
		}
		System.out.println("</RELATION>");
	}

	public ArrayList<String> getRelationStrings() {
		ArrayList<String> s = new ArrayList<String>();
		s.add("<RELATION>");
		for (int i = 0; i < relations.size(); i++) {
			s.add(relations.get(i).toString());
		}
		s.add("</RELATION>");
		return s;
	}

	public String cleanRawText() {
		StringBuffer buf = new StringBuffer();
		for (String t : rawSourceWithMentionTags) {
			buf.append(t);
			buf.append(" ");
		}
		this.xmlText = buf.toString().trim();

		// Remove XML tags
		this.cleanText = removeXMLTags(xmlText);

		// Standarize cleanText
		cleanText = Util.cleanLine(cleanText);
		cleanText = cleanText.trim();
		// FIXME: currently TA cannot accept tab
		// cleanText = cleanText.replaceAll("\\t+", " ");

		// Remove mention tags
		removeMentionTags();

		return cleanText;
	}

	
	private void removeMentionTags() {
		int i, j, tagStart, tagEnd;
		String xmlTag=null;

		HashMap<String, Mention> mentionsViaId = new HashMap<String, Mention>();
		for(Iterator<Mention> it=mentions.values().iterator(); it.hasNext();) {
			Mention m = it.next();
			mentionsViaId.put(m.getId(), m);
		}
		
		char[] annotatedBuffer = cleanText.toCharArray();
		char[] plainBuffer = new char[annotatedBuffer.length+1];
		for(i=0,j=0; i<annotatedBuffer.length; i++) {
			if( ((annotatedBuffer[i]=='<') && (annotatedBuffer[i+1]=='e')) ||
					((annotatedBuffer[i]=='<') && (annotatedBuffer[i+1]=='/') && (annotatedBuffer[i+2]=='e')) ||
					((annotatedBuffer[i]=='<') && (annotatedBuffer[i+1]=='h')) ||
					((annotatedBuffer[i]=='<') && (annotatedBuffer[i+1]=='/') && (annotatedBuffer[i+2]=='h')) ) {
				tagStart = i;
				while(annotatedBuffer[i]!='>') {
					i += 1;
				}
				tagEnd = i;
				xmlTag = cleanText.substring(tagStart, tagEnd+1);
				// myTags.add(new Pair<Integer, String>(new Integer(j), xmlTag));
				// Get mention id
				int i1 = xmlTag.indexOf("id=");
				i1 = xmlTag.indexOf("\"", i1) + 1;
				int i2 = xmlTag.indexOf("\"", i1);
				String mId = xmlTag.substring(i1, i2);
				if(mentionsViaId.containsKey(mId)) {
					if(xmlTag.startsWith("<e_")) { mentionsViaId.get(mId).setStartCharOffset(j); }
					else if(xmlTag.startsWith("</e_")) { mentionsViaId.get(mId).setEndCharOffset(j-1); }
					else if(xmlTag.startsWith("<h_")) { mentionsViaId.get(mId).setHeadStartCharOffset(j); }
					else if(xmlTag.startsWith("</h_")) { mentionsViaId.get(mId).setHeadEndCharOffset(j-1); }
				}
				else {
					System.out.println("mId="+mId+" is not contained in mentionsViaId; impossible. Exiting");
					System.exit(1);
				}
			}
			else {
				plainBuffer[j++] = annotatedBuffer[i];
			}

		}
		plainBuffer[j] = '\0';
		cleanText = (new String(plainBuffer)).substring(0, j);
		//System.out.println("CleanDoc.removeMentionTags: "+ cleanText);
		
		cleanMentions = new HashMap<Pair<Integer, Integer>, Mention>();
		for(Iterator<String> it=mentionsViaId.keySet().iterator(); it.hasNext();) {
			String mId = it.next();
			Mention m = mentionsViaId.get(mId);
			cleanMentions.put(new Pair<Integer, Integer>(m.getStartCharOffset(), m.getEndCharOffset()), m);
		}

		// Modifying the offsets of the mentions in the relations
		for (SemanticRelation rel : relations) {
			for (Pair<Integer, Integer> mKey : cleanMentions.keySet()) {
				Mention mention = cleanMentions.get(mKey);
				if (rel.getM1().getId().equals(mention.getId())) {
					rel.setM1(mention);
				}
				if (rel.getM2().getId().equals(mention.getId())) {
					rel.setM2(mention);
				}
			}
		}
	}

	private String removeXMLTags(String textWithXmlTags) {
		String s = textWithXmlTags.toString();

		for (String tag : Constants.setXMLTags) {

			// Dealing with "TEXT" later. This is for stripping off the junks
			// before the "TEXT" tag.
			if (tag.equals("TEXT"))
				continue;

			s = s.replaceAll("<" + tag + ".*?>", "");
			s = s.replaceAll("</" + tag + ">", "");
		}

		int pos = s.indexOf("<TEXT>");

		s = s.replaceAll("<" + "TEXT" + ".*?>", "");
		s = s.replaceAll("</" + "TEXT" + ">", "");

		String subBegin = s.substring(0, pos);
		String subAfter = s.substring(pos);

		subBegin = subBegin.replaceAll(".", " ");

		s = subBegin + subAfter;
		return s;
	}

	
	public void addRelationMentionTokenOffset(String mId, int i, int j) {
		for (SemanticRelation rel : relations) {
			Mention m = rel.getM1();
			if (m.getId().equals(mId)) {
				m.setStartTokenOffset(i);
				m.setEndTokenOffset(j);
				rel.setM1(m);
			}
			m = rel.getM2();
			if (m.getId().equals(mId)) {
				m.setStartTokenOffset(i);
				m.setEndTokenOffset(j);
				rel.setM2(m);
			}
		}
	}
	public void addRelationMentionHeadTokenOffset(String mId, int i, int j) {
		for (SemanticRelation rel : relations) {
			Mention m = rel.getM1();
			if (m.getId().equals(mId)) {
				m.setHeadStartTokenOffset(i);
				m.setHeadEndTokenOffset(j);
				rel.setM1(m);
			}
			m = rel.getM2();
			if (m.getId().equals(mId)) {
				m.setHeadStartTokenOffset(i);
				m.setHeadEndTokenOffset(j);
				rel.setM2(m);
			}
		}
	}

}
