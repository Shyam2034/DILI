

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SimilarityCheckWithinFP {
	//This program checks for similarity of fingerprints and merges two same fingerprints into one and changes the header to reflect the merger.
	//Currently this includes similarity within same type of fingerprints
	public static void main(String args[]) {

		try {
			XSSFWorkbook input = new XSSFWorkbook(new FileInputStream(new File("D:\\work\\Tasks\\Data_13062022\\955\\VarianceFiltered\\Raw_dilist_processed_drugs_SubstructureFP_955_VF.xlsx")) );
			//Change the above filename accordingly.  Excel was created for ease of use.
			String currentSheet = "Variance";

			XSSFSheet fingerprints = input.getSheet(currentSheet);//Contains headers and first column with compound names
			double cellValue1 = 0, cellValue2 = 0; //Temporary storage value
			
			ArrayList<Integer> duplicateFingerprints = new ArrayList<Integer>();
			HashMap<Integer, String> newHeaders = new HashMap<Integer, String> ();
			
			boolean similar = true;
			int row = 0;
			for(int col1 = 1; col1 < fingerprints.getRow(0).getLastCellNum(); col1++) {//Iterates over each column at once
				if(duplicateFingerprints.contains(col1)) {
					continue;
				}
				for(int col2 = col1 + 1; col2 < fingerprints.getRow(0).getLastCellNum() ; col2++) {//Iterates over each row at once
					if(duplicateFingerprints.contains(col2)) {
						continue;
					}
					similar = true;
					try {
						for(row = 1; row <= fingerprints.getLastRowNum(); row++) {
							//Ignoring row = 0 as it contains headers

							cellValue1 = fingerprints.getRow(row).getCell(col1).getNumericCellValue();
							cellValue2 = fingerprints.getRow(row).getCell(col2).getNumericCellValue();
							if(cellValue1 == cellValue2) {//Checks if each value for a compound are same
								continue;
							}
							else {
								similar = false;
								break;
							}
						}
					}catch(Exception e) {
						//This is to catch non numeric cell values
						//We need to ignore this particular descriptors or fingerprint. Currently we may not get these errors
						System.out.println("Error in getting double value for row: "+ row +", and col2: "+ col1 + " or col2: " + col2);
						continue;
					}
					if(similar) {
						duplicateFingerprints.add(col2);//Don't use this duplicate column for further processing as it is same as col1
						if(newHeaders.containsKey(col1)) {
							newHeaders.put(col1, newHeaders.get(col1) 
									+"||" + fingerprints.getRow(0).getCell(col2).getStringCellValue().toString()  );
						}else {
							newHeaders.put(col1, fingerprints.getRow(0).getCell(col1).getStringCellValue().toString() 
									+"||" + fingerprints.getRow(0).getCell(col2).getStringCellValue().toString()  );
						}
					}
				}
				
			}


			PrintWriter checkedFingerprints = new PrintWriter("D:\\work\\Tasks\\Data_13062022\\955\\WithinSimilarity\\Raw_dilist_processed_drugs_SubstructureFP_955_VF_WS.txt");
			//Printing Headers first
			for(int col = 0; col < fingerprints.getRow(0).getLastCellNum(); col++) {
				if(!duplicateFingerprints.contains(col)) {
					if(newHeaders.containsKey(col)) {
						checkedFingerprints.print(newHeaders.get(col)+",");
					}else {
						checkedFingerprints.print(fingerprints.getRow(0).getCell(col).getStringCellValue().toString()+",");
					}
				}
			}
			checkedFingerprints.print("\n");
			//Printing cellValues
			
			for(row = 1; row <= fingerprints.getLastRowNum(); row++) {
				//Below line writes compound name				
				checkedFingerprints.print(fingerprints.getRow(row).getCell(0).getStringCellValue().toString());
				for(int col = 1; col < fingerprints.getRow(0).getLastCellNum(); col++) {
					if(!duplicateFingerprints.contains(col)) {
						checkedFingerprints.print(","+fingerprints.getRow(row).getCell(col).getNumericCellValue());
					}
				}
				checkedFingerprints.print("\n");
			}
			System.out.println("Number of similar fingerprints: "+ duplicateFingerprints.size());
			checkedFingerprints.flush();
			checkedFingerprints.close();
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
