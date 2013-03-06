package edu.illinois.cs.cogcomp.illinoisRE.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.io.LineIO;

public class WordClusterManager {
	private Map<String,String> map = new HashMap<String, String>();
	private Map<String, String> bit10 = new HashMap<String, String>();
	
	public WordClusterManager() {
		BufferedReader br = null;
		InputStream is = ClassLoader.getSystemResourceAsStream("edu/illinois/cs/cogcomp/illinoisRE/common/brown_clusters");
		if(is!=null) {		// get as resource 
			br = new BufferedReader(new InputStreamReader(is)); 
		}
		else {
			System.out.println("br is null!");
		}
		String line;
		List<String> lines = new ArrayList<String>();
		
		try {
			while((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		
		/*
		//List<String> lines = null;
		try {
			lines = LineIO.read(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		*/
		
		for (String s: lines){
			String[] list = s.split("\\s+");
			
			map.put(list[1], list[0]);
			
			if(list[0].length() >= 10) {
				bit10.put(list[1], list[0].substring(0, 10));
			}
		}
		
		
		for (String s: lines){
			String[] list = s.split("\\s+");
			
			if(!bit10.containsKey(list[1].toLowerCase())) {
				if(list[0].length() >= 10) {
					bit10.put(list[1].toLowerCase(), list[0].substring(0, 10));
				}
			}
		}
	}
	
	public String getCluster(String word){
		if (map.containsKey(word))
			return map.get(word);
		else
			return "**********-NONE-CLUSTER--";
	}
	
	public String getCluster(String word, int bitNum) {
		String bitString = null;
		String targetWord = word.toLowerCase();
		
		if(bitNum==10) {
			if(bit10.containsKey(targetWord))
				bitString = bit10.get(targetWord);
			else {
				bitString = new String("**********-NONE-CLUSTER--");
			}
		}
		
		return bitString;
	}
}
