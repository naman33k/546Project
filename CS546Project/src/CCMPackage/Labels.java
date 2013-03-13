package CCMPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Labels {
	public static Set<String> coarseEntityLabels = new HashSet<String>();
	static{
		try{
			//System.out.println("Hello");
			BufferedReader br = new BufferedReader(new FileReader("CoarseEntityLabels"));
			String line = br.readLine();
			while(line!=null){
				coarseEntityLabels.add(line.trim());
				line = br.readLine();
			}
			br.close();
			//System.out.println(coarseEntityLabels.toString());
		}
		catch(Exception e){
			System.out.println("Coarse Entity Files Not Found");
		}
	}
	public static List<String> coarseEntityList = new ArrayList<String>();
	static{
		coarseEntityList.addAll(coarseEntityLabels);		
	}
	
	public static Set<String> POSLabels = new HashSet<String>();
	static{
		try{
			BufferedReader br = new BufferedReader(new FileReader("POSLabels"));
			String line = br.readLine();
			while(line!=null){
				POSLabels.add(line.trim());				
				line = br.readLine();
			}
			br.close();
		}
		catch(Exception e){
			System.out.println("POS Files Not Found");
		}		
	}
	public static List<String> POSList = new ArrayList<String>();
	static{
		POSList.addAll(POSLabels);		
	}
	public static Set<String> fineRelationLabels = new HashSet<String>();
	static{
		try{
			//System.out.println("Hello");
			BufferedReader br = new BufferedReader(new FileReader("fineRelationLabels"));
			String line = br.readLine();
			while(line!=null){
				fineRelationLabels.add(line.trim());
				line = br.readLine();
			}
			br.close();
			//System.out.println(coarseEntityLabels.toString());
		}
		catch(Exception e){
			System.out.println("Fine Relation Files Not Found");
		}
	}
	public static List<String> fineRelationList = new ArrayList<String>();
	static{
		fineRelationList.addAll(fineRelationLabels);		
	}
	
	//Relations
	
	public static int mapRelationStoI(String s){
		return fineRelationList.indexOf(s);
	}
	public static String mapRelationItoS(int i){
		return fineRelationList.get(i);
	}
	public static int numRelationFineLabels(){
		return fineRelationList.size();
	}
	
	//Entity
	
	public static int mapEntityStoI(String s){
		return coarseEntityList.indexOf(s);
	}
	public static String mapEntityItoS(int i){
		return coarseEntityList.get(i);
	}
	public static int numEntityCoarseLabels(){
		return coarseEntityList.size();
	}
	
	
	//POS
	public static int mapPOSStoI(String s){
		return POSList.indexOf(s);
	}
	public static String mapPOSItoS(int i){
		return POSList.get(i);
	}
	public static int numPOSFineLabels(){
		return POSList.size();
	}
}
