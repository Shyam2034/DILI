

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SimilarityCheckBetweenFP {
	//This program checks for similarity of fingerprints and merges two same fingerprints into one and changes the header to reflect the merger.
	//Currently this includes similarity within same type of fingerprints
	public static void main(String args[]) {

		try {
			XSSFWorkbook input1 = new XSSFWorkbook(new FileInputStream(new File("D:\\work\\Tasks\\Data_13062022\\955\\BetweenSimilarity\\FP_BS8.xlsx")) );
			//Change the above filename accordingly.  Excel was created for ease of use.
			String currentSheet1 = "Sheet1";
			XSSFSheet fingerprints1 = input1.getSheet(currentSheet1);//Contains headers and first column with compound names
			
			XSSFWorkbook input2 = new XSSFWorkbook(new FileInputStream(new File("D:\\work\\Tasks\\Data_13062022\\955\\WithinSimilarity\\Raw_dilist_processed_drugs_ECFP_955_VF_WS.xlsx")) );
			String currentSheet2 = "Sheet1";
			XSSFSheet fingerprints2 = input2.getSheet(currentSheet2);//Contains headers and first column with compound names
			
			//Change the below variable as per 
			int compoundNameColNum = 0;//Assumed that first column contains compound name.
			if(fingerprints1.getLastRowNum() != fingerprints2.getLastRowNum()) {
				System.out.println("Number of compounds in both sheets not equal, program may not work well! ");
				/*
				 * input1.close(); input2.close(); return;
				 */
			}
			//TODO change the name of output file accordingly
			PrintWriter checkedFingerprints = new PrintWriter("D:\\work\\Tasks\\Data_13062022\\955\\BetweenSimilarity\\FP_BS9.txt");
			
			
			//Fetching corresponding rows for input2
			HashMap<String, Integer> secondInputCompoundRowsInfo = getCompoundRows(fingerprints2, compoundNameColNum);
			//Similar fingerprint will be removed from second file and the header in first input file will be renamed to reflect both the similar fingerprints.
			ArrayList<Integer> duplicateFingerprints = new ArrayList<Integer>();//For second input file
			HashMap<Integer, String> newHeaders = new HashMap<Integer, String> ();//For first input file
			
			boolean similar = true;
			int row = 0, rowNum2 = 0;
			double cellValue1 = 0, cellValue2 = 0; //Temporary storage value
			String compoundName = "";
			for(int col1 = 1; col1 < fingerprints1.getRow(0).getLastCellNum(); col1++) {//Iterates over each column at once
				for(int col2 = 1; col2 < fingerprints2.getRow(0).getLastCellNum() ; col2++) {//Iterates over each row at once
					if(duplicateFingerprints.contains(col2)) {
						continue;
					}
					similar = true;
					try {
						//Ignoring row = 0 as it contains headers
						for(row = 1; row <= fingerprints1.getLastRowNum(); row++) {
							compoundName = fingerprints1.getRow(row).getCell(compoundNameColNum).getStringCellValue().toString().trim();
							if(!secondInputCompoundRowsInfo.containsKey(compoundName)) {
								//Better handling of this scenario is needed.
								//System.out.println("Compound Name not found, "+compoundName +" in inputfile2 may not get accurate results");
								continue;
							}
							rowNum2 = secondInputCompoundRowsInfo.get(compoundName);
							cellValue1 = fingerprints1.getRow(row).getCell(col1).getNumericCellValue();
							cellValue2 = fingerprints2.getRow(rowNum2).getCell(col2).getNumericCellValue();
							if(cellValue1 == cellValue2) {//Checks if each value for a compound are same
								continue;
							}
							else {
								similar = false;
								break;
							}
						}
					}catch(Exception e) {
						System.out.println("Error in getting double value for row: "+ row +", and col1: "+ col1 + " or col2: " + col2);
						e.printStackTrace();
						continue;
					}
					if(similar) {
						duplicateFingerprints.add(col2);//Don't use this duplicate column for further processing as it is same as col1
						if(newHeaders.containsKey(col1)) {
							newHeaders.put(col1, newHeaders.get(col1) 
									+"||" + fingerprints2.getRow(0).getCell(col2).getStringCellValue().toString()  );
						}else {
							newHeaders.put(col1, fingerprints1.getRow(0).getCell(col1).getStringCellValue().toString() 
									+"||" + fingerprints2.getRow(0).getCell(col2).getStringCellValue().toString()  );
						}
					}
				}
				
			}
			
			//Printing Headers first of file one
			for(int col = 0; col < fingerprints1.getRow(0).getLastCellNum(); col++) {
				if(newHeaders.containsKey(col)) {
					checkedFingerprints.print(newHeaders.get(col)+",");
				}else {
					checkedFingerprints.print(fingerprints1.getRow(0).getCell(col).getStringCellValue().toString()+",");
				}
			}
			//Printing headers of second file, starting from column 1 as first column (=0) contains compounds names
			for(int col = 1; col < fingerprints2.getRow(0).getLastCellNum(); col++) {
				if(!duplicateFingerprints.contains(col)) {
					checkedFingerprints.print(fingerprints2.getRow(0).getCell(col).getStringCellValue().toString()+",");
				}
			}
			checkedFingerprints.print("\n");
			
			//Printing cellValues			
			for(row = 1; row <= fingerprints1.getLastRowNum(); row++) {
				compoundName = fingerprints1.getRow(row).getCell(compoundNameColNum).getStringCellValue().toString().trim();
				if(!secondInputCompoundRowsInfo.containsKey(compoundName)) {
					//Better handling of this scenario is needed.
					continue;
				}
				//Below line writes compound name				
				checkedFingerprints.print(fingerprints1.getRow(row).getCell(compoundNameColNum).getStringCellValue().toString());
				for(int col = 1; col < fingerprints1.getRow(0).getLastCellNum(); col++) {
					checkedFingerprints.print(","+fingerprints1.getRow(row).getCell(col).getNumericCellValue());
				}
				rowNum2 = secondInputCompoundRowsInfo.get(compoundName);
				for(int col = 1; col < fingerprints2.getRow(0).getLastCellNum(); col++) {
					if(!duplicateFingerprints.contains(col)) {
						checkedFingerprints.print(","+fingerprints2.getRow(rowNum2).getCell(col).getNumericCellValue());
					}
				}
				checkedFingerprints.print("\n");
			}
			System.out.println("Number of similar fingerprints: "+ duplicateFingerprints.size());
			checkedFingerprints.flush();
			checkedFingerprints.close();
			input1.close();
			input2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static HashMap<String, Integer> getCompoundRows(XSSFSheet fingerprints, int compoundColNum) {
		if(fingerprints == null) {
			System.out.println("Fingerpinrts sheet empty!");
			return  new HashMap<String, Integer>  ();//This will return a empty map
		}
		HashMap<String, Integer> compoundRowNums = new HashMap<String, Integer>  ();
		//Assuming the compound names are in column one (zero in java) and the excel file contains header.
		for(Row currentRow : fingerprints) {
			try {
				compoundRowNums.put(currentRow.getCell(compoundColNum).getStringCellValue().toString().trim(), currentRow.getRowNum());
			}catch(Exception e) {
				System.out.println("Please check assumptions for the function which may not be met in row: " + currentRow.getRowNum());
			}
		}
		return compoundRowNums;
	}

}
