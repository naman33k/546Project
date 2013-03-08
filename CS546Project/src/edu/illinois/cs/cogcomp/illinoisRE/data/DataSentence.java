package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;

public class DataSentence {

	public ArrayList<Mention> mentions = new ArrayList<Mention>();
	public ArrayList<SemanticRelation> relations = new ArrayList<SemanticRelation>();
	public Sentence sentence;
	
	
	public DataSentence(Sentence s, ArrayList<Mention> mentions, ArrayList<SemanticRelation> relations) {
	    	
		int myId = s.getSentenceId();
		
		for(Mention m: mentions) {
			if(m.getSentId() == myId) {
				this.mentions.add(m);
			}
		}
		
		for(SemanticRelation r: relations) { //if first mention is in, then relation is in
			if(r.getM1().getSentId() == myId) {
                r.setSentenceId(myId);
				r.setSentence(s);
				this.relations.add(r);
			}
		}
		
		sentence = s;
		setPosToMentions();
	}
	public void setPosToMentions(){
		for(Mention m:mentions){
			int headTokenOffset = m.getHeadStartTokenOffset();
			int headTokenId = headTokenOffset;
			//System.out.println(sentence.getStartSpan());
			//System.out.println(headTokenOffset);
			TextAnnotation t = sentence.getView(ViewNames.POS).getTextAnnotation();
			m.setHeadPos(WordHelpers.getPOS(t, headTokenId));									
		}
	    		
	}
	public String toString() {
		String s = sentence.getText();
		s += "\n";
		s += sentence.getStartSpan()+" "+ sentence.getEndSpan();
		s += "\n";
		for(Mention m: mentions) {
			s += m.toString() +"\n";
		}
		
		for(SemanticRelation r: relations) {
			s += r.toString() +"\n";
		}
		
		return s;
	}
	
	public String convertToRelationFeatures(LexManager m){
		String s = "";
		for(SemanticRelation r: relations) {
			s += r.convertToFeatureString(m);
		}
		return s;
	}
	
}
