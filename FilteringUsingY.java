
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FilteringUsingY {

	public static void main(String args[]) {

		try {
			XSSFWorkbook input = new XSSFWorkbook(new FileInputStream(
					new File("D:\\work\\Tasks\\Data_13062022\\955\\BetweenSimilarity\\FP_BS9_Y2.xlsx")));
			// Change the above filename accordingly. Excel was created for ease of use.
			String currentSheet = "Sheet1";

			XSSFSheet fingerprints = input.getSheet(currentSheet);// Contains headers and first column with compound
																	// names
			double[][] continT = new double[2][2];
			double chi = 0, fisherP = 0, chiSquareThreshold = 3.84, fisherHigherLimit = 0.05;// TO be discussed and
																								// changed
			int currentFingerprintVal = 0, compoundYValue = 0, chiMinCV = 5, yColNum = 1;
			ArrayList<Integer> removedFingerprints = new ArrayList<Integer>();
			PrintWriter yStatistics = new PrintWriter(
					"D:\\work\\Tasks\\Data_13062022\\955\\BetweenSimilarity\\FinalLevel2_Statistics.txt");
			yStatistics.println("Fingerprint,ChiTestStatistic,FisherP");
			
			System.out.println("Last column No:"+fingerprints.getRow(0).getLastCellNum());
			for (int col = 2; col < fingerprints.getRow(0).getLastCellNum(); col++) {
				fisherP = 1;
				chi = 0;
				try {
					// iterating over fingerprint values
					for (int i = 0; i < 2; i++) {
						for (int j = 0; j < 2; j++) {
							continT[i][j] = 0;
						}
					}
					for (int row = 1; row < fingerprints.getLastRowNum() + 1; row++) {
						currentFingerprintVal = (int) fingerprints.getRow(row).getCell(col).getNumericCellValue();
						compoundYValue = (int) fingerprints.getRow(row).getCell(yColNum).getNumericCellValue();
						continT[currentFingerprintVal][compoundYValue] += 1;
					}
					if (continT[0][0] > chiMinCV && continT[0][1] > chiMinCV && continT[1][0] > chiMinCV
							&& continT[1][1] > chiMinCV) {
						chi = StatisticalTests.getChi(continT, fingerprints.getLastRowNum());

					} else {
						fisherP = StatisticalTests.getFisherExact(continT);
					}
					if ((chi > chiSquareThreshold) || fisherP < fisherHigherLimit) {
						// Add it
					} else {
						// System.out.println(col +"," + chi + "," + fisherP);
						removedFingerprints.add(col);
					}
					yStatistics.println(fingerprints.getRow(0).getCell(col).getStringCellValue().toString()// Value of
																											// fingerprints
							+ "," + chi + "," + fisherP);
				} catch (Exception e) {
					System.out.println("Error processing fingerprint: " + col);
					e.printStackTrace();
				}
			}
			yStatistics.flush();
			yStatistics.close();

			PrintWriter filteredFingerprints = new PrintWriter(
					"D:\\work\\Tasks\\Data_13062022\\955\\BetweenSimilarity\\Level2Filtered.txt");
			// Printing Headers first
			for (int col = 0; col < fingerprints.getRow(0).getLastCellNum(); col++) {
				if (!removedFingerprints.contains(col)) {
					filteredFingerprints
							.print(fingerprints.getRow(0).getCell(col).getStringCellValue().toString() + ",");
				}
			}
			filteredFingerprints.print("\n");
			// Printing cellValues

			for (int row = 1; row <= fingerprints.getLastRowNum(); row++) {
				// Below line writes compound name
				filteredFingerprints.print(fingerprints.getRow(row).getCell(0).getStringCellValue().toString());
				for (int col = 1; col < fingerprints.getRow(0).getLastCellNum(); col++) {
					if (!removedFingerprints.contains(col)) {
						filteredFingerprints.print("," + fingerprints.getRow(row).getCell(col).getNumericCellValue());
					}
				}
				filteredFingerprints.print("\n");
			}
			System.out.println("Number of similar fingerprints: " + removedFingerprints.size());
			filteredFingerprints.flush();
			filteredFingerprints.close();

			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
