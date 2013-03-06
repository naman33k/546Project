package edu.illinois.cs.cogcomp.illinoisRE.common;

import java.io.Serializable;
import java.util.List;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.MyCuratorClient;

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class Document implements Serializable{
	//private String id = null;
	public TextAnnotation ta = null;
	private List<SemanticRelation> relations;
	
	/**
	 * 
	 */
	public Document() {
		//id = null;
		ta = null;
	}
	
	/**
	 * 
	 */
	public Document(TextAnnotation ta) {
		this.ta = ta;
	}

	/**
	 * @return the ta
	 */
	public TextAnnotation getTextAnnotation() {
		return ta;
	}
	
	public void setRelations(List<SemanticRelation> relations) {
		this.relations = relations;
	}
	
	public List<SemanticRelation> getRelations() {
		return relations;
	}
	
	
}
