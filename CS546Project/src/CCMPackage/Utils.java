package CCMPackage;

public class Utils {
	public static void print2DArray(double[][] arr){
		for(int i=0;i<arr.length;i++){
			for(int j=0;i<arr.length;j++){
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static String doubleArrayToCSString(double[] arr){
		String toRet = "";
		int len = arr.length;
		for(int i=0;i<len-1;i++){
			toRet += arr[i] + ",";
		}
		toRet += arr[len-1];
		return toRet;
	}
	public static String double2DArrayToCSString(double[][] arr){
		String toRet = "";
		int len = arr.length;		
		for(int i=0;i<len-1;i++){
			toRet += doubleArrayToCSString(arr[i]) + "\n";
		}
		toRet += doubleArrayToCSString(arr[len-1]);
		return toRet;
	}
}
