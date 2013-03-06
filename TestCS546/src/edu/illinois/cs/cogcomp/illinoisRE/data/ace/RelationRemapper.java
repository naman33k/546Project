package edu.illinois.cs.cogcomp.illinoisRE.data.ace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.illinoisRE.common.IOManager;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class RelationRemapper {
	
	public static HashMap<Pair<String, String>, Pair<String, String>> readRelationTypesFromXmlFile(String xmlfile) {
		String line=null;
		int i1, i2;
		String docid=null, rid=null, coarseRelLabel, fineRelLabel=null;
		HashMap<Pair<String, String>, Pair<String, String>> relLabels = new HashMap<Pair<String, String>, Pair<String, String>>();
		
		ArrayList<String> lines = IOManager.readLines(xmlfile);
		for(int lineIndex=0; lineIndex<lines.size(); lineIndex++) {
			line = lines.get(lineIndex);
			if(line.startsWith("<DOC id=")) {
				i1 = line.indexOf("\"", line.indexOf(" id="))+1;
				i2 = line.indexOf("\"", i1);
				docid = line.substring(i1, i2);
			}
			if(line.startsWith("<r_id=")) {
				i1 = line.indexOf("\"")+1;
				i2 = line.indexOf("\"", i1);
				rid = line.substring(i1, i2);
				
				i1 = line.indexOf("r=\"");
				i1 = line.indexOf("\"", i1)+1;
				i2 = line.indexOf("\"", i1);
				fineRelLabel = line.substring(i1, i2);
				
				if(fineRelLabel.endsWith(":")) {
					fineRelLabel = fineRelLabel.substring(0, fineRelLabel.length()-1);
				}
				
				if(fineRelLabel.indexOf(":")==-1) {
					coarseRelLabel = fineRelLabel;
				}
				else {
					coarseRelLabel = fineRelLabel.substring(0, fineRelLabel.indexOf(":"));
					fineRelLabel = fineRelLabel.substring(fineRelLabel.indexOf(":")+1);
				}
				
				relLabels.put(new Pair<String, String>(docid, rid), new Pair<String, String>(coarseRelLabel, fineRelLabel));
			}
		}
		
		/*
		for(Iterator<Pair<String, String>> it=relLabels.keySet().iterator(); it.hasNext();) {
			Pair<String, String> key = it.next();
			Pair<String, String> label = relLabels.get(key);
			System.out.println("docid="+key.getFirst()+" rid="+key.getSecond()+" coarseLabel="+label.getFirst()+" fineLabel="+label.getSecond());
		}
		*/
		
		return relLabels;
	}
	
	
	
	public static ArrayList<String> modifyRelationLabelsForFile(String xmlfile, String docid, HashMap<Pair<String, String>, Pair<String, String>> relLabels) {
		ArrayList<String> lines = IOManager.readLinesWithoutTrimming(xmlfile);
		String line=null, coarseRelLabel=null, fineRelLabel=null, rid=null;
		int i1, i2;
		ArrayList<String> outputLines = new ArrayList<String>();
		
		for(int lineIndex=0; lineIndex<lines.size(); lineIndex++) {
			line = lines.get(lineIndex);
			if(line.indexOf("<relation ID=")!=-1) {
				rid=null;
				coarseRelLabel = null;
				fineRelLabel = null;
				boolean hasRelationMention = false;
				
				if(line.indexOf(" TYPE=")!=-1) {
					i1 = line.indexOf("\"", line.indexOf(" TYPE="))+1;
					i2 = line.indexOf("\"", i1);
					coarseRelLabel = line.substring(i1, i2);
				}
				if(line.indexOf(" SUBTYPE=")!=-1) {
					i1 = line.indexOf("\"", line.indexOf(" SUBTYPE="))+1;
					i2 = line.indexOf("\"", i1);
					fineRelLabel = line.substring(i1, i2);
				}
				else {
					fineRelLabel = coarseRelLabel;
				}
				
				HashSet<String> coarseLabels = new HashSet<String>();
				HashSet<String> fineLabels = new HashSet<String>();
				List<String> relationLines = new ArrayList<String>();
				while(line.trim().compareTo("</relation>")!=0) {
					if(line.indexOf("relation_mention ID=")!=-1) {
						hasRelationMention = true;
						i1 = line.indexOf("\"", line.indexOf("relation_mention ID="))+1;
						i2 = line.indexOf("\"", i1);
						rid = line.substring(i1, i2);
						// following is for ace2005
						if(rid.contains("-R")) {
							rid = rid.substring(rid.indexOf("-R")+2);
						}
						
						if(relLabels.containsKey(new Pair<String, String>(docid, rid))) {
							Pair<String, String> coarseFineLabel = relLabels.get(new Pair<String, String>(docid, rid));
							coarseLabels.add(coarseFineLabel.getFirst());
							fineLabels.add(coarseFineLabel.getSecond());
						}
						//else {
						//	if(coarseRelLabel.compareTo("DISC")!=0) {
								// I will let the TYPE and SUBTYPE remain as they are; but I'll need to manually go over to verify the labels
						//		System.out.println("ERROR: " + docid + " " + rid + " is not found in relLabels; fineRelLabel="+fineRelLabel);
						//	}
						//}
					}
					
					relationLines.add(line);
					lineIndex += 1;
					line = lines.get(lineIndex);
				}
				relationLines.add(line);
				
				// if exactly equals 1, get new coarseRelLabel and fineRelLabel
				// if == 0 and != DISC, I will need to manually verify
				// if > 1, I will need to manually separate the relation mentions
				if(coarseLabels.size()==1 && fineLabels.size()==1) {
					coarseRelLabel = null;
					for(Iterator<String> it=coarseLabels.iterator(); it.hasNext();) {
						coarseRelLabel = it.next();
					}
					fineRelLabel = null;
					for(Iterator<String> it=fineLabels.iterator(); it.hasNext();) {
						fineRelLabel = it.next();
					}
				}
				if(hasRelationMention) {
					if(coarseLabels.size()==0 && fineLabels.size()==0 && coarseRelLabel.compareTo("DISC")!=0) {
						System.out.println("ERROR: docid="+docid+" rid="+rid+"; if == 0 and != DISC, I will need to manually verify; "+coarseRelLabel+","+fineRelLabel);
					}
					if(coarseLabels.size()>1 || fineLabels.size()>1) {
						System.out.println("ERROR: docid="+docid+" rid="+rid+"; if > 1, I will need to manually separate the relation mentions");
					}
				}
				
				for(int i=0; i<relationLines.size(); i++) {
					if(relationLines.get(i).indexOf("<relation ID=")!=-1) {
						// output the revised relation labels
						String newLine = null;
						if(relationLines.get(i).indexOf(" TYPE=")!=-1) {
							i1 = relationLines.get(i).indexOf("\"", relationLines.get(i).indexOf(" TYPE="))+1;
							i2 = relationLines.get(i).indexOf("\"", i1);
							newLine = relationLines.get(i).substring(0, i1) + coarseRelLabel + relationLines.get(i).substring(i2);
						
							if(coarseRelLabel.compareTo(fineRelLabel)==0) {		// then I just end with TYPE
								i1 = newLine.indexOf("\"", newLine.indexOf(" TYPE="))+1;
								i2 = newLine.indexOf("\"", i1);
								newLine = newLine.substring(0, i2+1) + ">";
							}
							else {
								if(newLine.indexOf(" SUBTYPE=")!=-1) {
									i1 = newLine.indexOf("\"", newLine.indexOf(" SUBTYPE="))+1;
									i2 = newLine.indexOf("\"", i1);
									newLine = newLine.substring(0, i1) + fineRelLabel + newLine.substring(i2);
								}
							}
						}
						if(newLine==null) {
							System.out.println("ERROR: line has no TYPE: "+ relationLines.get(i));
						}
						else { 
							outputLines.add(newLine);
						}
					}
					else {
						outputLines.add(relationLines.get(i));
					}
				}
				
			}
			else {
				outputLines.add(line);
			}
		}
		
		return outputLines;
	}
	
	// the xmlfile is ace04+05.column
	public static void modifyRelationLabelsForDir(String xmlfile, String inputdir, String outputdir) {
		String[] filenames = IOManager.listDirectory(inputdir);
		String filename=null, docid;
		
		HashMap<Pair<String, String>, Pair<String, String>> relLabels = readRelationTypesFromXmlFile(xmlfile);
		for(int fileIndex=0; fileIndex<filenames.length; fileIndex++) {
			filename = filenames[fileIndex];
			if(filename.endsWith(".apf.xml")) {
				docid = filename.substring(0, filename.indexOf(".apf.xml"));
				
				ArrayList<String> outputLines = modifyRelationLabelsForFile(inputdir+"/"+filename, docid, relLabels);
				IOManager.writeLinesAddingReturn(outputLines, outputdir+"/"+filename);
			}
			
		}
	}
	
	public static void main(String args[]) {
		String xmlfile = args[0];
		String inputdir = args[1];
		String outputdir = args[2];
		
		modifyRelationLabelsForDir(xmlfile, inputdir, outputdir);
		
	}
	
}

