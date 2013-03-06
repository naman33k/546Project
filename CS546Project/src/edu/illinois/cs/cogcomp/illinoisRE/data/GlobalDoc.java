package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;

public class GlobalDoc {

	public Document doc;
	public CleanDoc cdoc;
	public ArrayList<DataSentence> sentences = new ArrayList<DataSentence>();
	public ArrayList<Mention> mentions = new ArrayList<Mention>();
	public ArrayList<SemanticRelation> relations = new ArrayList<SemanticRelation>();
	
	//list of sentences
	
	public GlobalDoc(Document doc, CleanDoc cdoc) {
		this.doc = doc;
		this.cdoc = cdoc;
	}
	
	public void process() {
		//get Sentences

		HashMap<Pair<Integer,Integer>,Mention> map = cdoc.getCleanMentions();
		Collection<Mention> m = map.values();
		Iterator<Mention> it = m.iterator();
		while(it.hasNext()) {
			//System.out.println(it.next());
			mentions.add(it.next());
		}
		
		for(Mention men: mentions) {
			int s = doc.ta.getTokenIdFromCharacterOffset(men.getStartCharOffset());
			int e = doc.ta.getTokenIdFromCharacterOffset(men.getEndCharOffset());
			int sentId = doc.ta.getSentenceId(s);
			int hs = doc.ta.getTokenIdFromCharacterOffset(men.getHeadStartCharOffset());
			int he = doc.ta.getTokenIdFromCharacterOffset(men.getHeadEndCharOffset());
			men.update(hs, he, s, e, sentId);
		}
		
		relations = (ArrayList) cdoc.getRelations();
		
		
		for(int i = 0; i < doc.ta.getNumberOfSentences(); i++) {
			Sentence s = doc.ta.getSentence(i);		     
			sentences.add(new DataSentence(s, mentions, relations));
		}


	}
	
	public String toString() {
		String r = "";
		
		for(DataSentence s: sentences) {
			r += s.toString() + "\n";
		}
		
		return r;
	}
	
	public String convertToRelationFeatures(){
		String r="";
		for(DataSentence s: sentences) {
			r += s.convertToRelationFeatures() + "\n";
		}
		
		return r;
	}
}
