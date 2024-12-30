package docCompare;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class excel_Reader_Pro {
	
	
	 public static void main(String[] args) {
	        String filePath = "C:\\Users\\chetan.patel\\Documents\\docx\\Book123.xlsx";
	        String sheetName = "Sheet1";
	        String reportFilePath = "C:\\Users\\chetan.patel\\Documents\\docx\\PDF_report.csv";

	        try (Workbook workbook = new XSSFWorkbook(filePath);
	             FileWriter writer = new FileWriter(reportFilePath)) {

	            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
	            DataFormatter dataFormatter = new DataFormatter();
	            Iterator<Row> rowIterator = sheet.iterator();

	            Map<String, Map<String, String>> hashMap = new LinkedHashMap<>();
	            Map<String, String> reportData = new HashMap<>(); // Store report for each PDF file

	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                Cell keyCell = row.getCell(0);
	                Cell valueCell = row.getCell(1);
	                Cell ignoreCell = row.getCell(2);
	                String filePaths1 = dataFormatter.formatCellValue(keyCell);
	                String filePaths2 = dataFormatter.formatCellValue(valueCell);
	                String ignoreFile = dataFormatter.formatCellValue(ignoreCell);

	                if (!hashMap.containsKey(filePaths1)) {
	                    hashMap.put(filePaths1, new HashMap<>());
	                }
	                Map<String, String> innerMap = hashMap.get(filePaths1);
	                innerMap.put(filePaths2, ignoreFile);
	            }
	            
	           
	            
                String Line1 = "File 1" + "," +"File 2" + "," +  " Result Pdf Name" + "," + "Difference Found" + "," + "\n";
                writer.write(Line1);

	            int counter = 1;
	            int count = 1;
	            for (Map.Entry<String, Map<String, String>> entry : hashMap.entrySet()) {
	                String file1 = entry.getKey();
	                Map<String, String> innerMap = entry.getValue();

	                System.out.println("filePaths1 = " + file1);
	                
	              
	              
	                for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
	                    String file2 = innerEntry.getKey();
	                    String ignoreFile = innerEntry.getValue();

	                    String resultPdf = "C:\\Users\\chetan.patel\\Documents\\docx\\New folder\\result\\result" + counter + ".pdf";
	                    counter++;
	                    String Result = "result" + count + ".pdf";
		                   count ++;

	                    System.out.println("    filePaths2 = " + file2);
	                    System.out.println("    ignoreFile = " + ignoreFile);

	                    // Call your pdfComparator method from pdfCompare class
	                    boolean differencesFound = new docComparator(file1, file2).withIgnore(ignoreFile).compare().hasDifferenceInExclusion();
	                  //   Collection<PageArea> diff_Cordinates = new docComparator(file1, file2).withIgnore(ignoreFile).compare().getDifferences();
	                    new docComparator(file1, file2).withIgnore(ignoreFile).compare().writeTo(resultPdf);
	                    System.out.println("Process is completed");
	                    
	                    // Generate the report
	                    String reportLine = file1 + "," + file2 + "," +Result+"," +(differencesFound ? "Yes" : "No") +"," + "\n";
	                    writer.write(reportLine);

	                    // Store the report for the PDF file
	                    String pdfFileName = file2.substring(file2.lastIndexOf("\\") + 1); // Extract the PDF file name
	                    reportData.put(pdfFileName, differencesFound ? "Yes" : "No");
	                }
	            }

	            // Print the report data
	            for (Map.Entry<String, String> entry : reportData.entrySet()) {
	                System.out.println("PDF File: " + entry.getKey() + ", Differences Found: " + entry.getValue());
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}