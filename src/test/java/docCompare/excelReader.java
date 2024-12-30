package docCompare;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

public class excelReader {
	
	public static void main(String[] args) {
	    String filePath = "C:\\Users\\chetan.patel\\Documents\\docx\\Book123.xlsx";
	    String sheetName = "Sheet1";

	    try (FileInputStream fis = new FileInputStream(filePath);
	         Workbook workbook = new XSSFWorkbook(fis)) {

	        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
	        DataFormatter dataFormatter = new DataFormatter();
	        Iterator<Row> rowIterator = sheet.iterator();

	        Map<String, Map<String, String>> hashMap = new LinkedHashMap<>();

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

	        int counter = 1;
	        for (Map.Entry<String, Map<String, String>> entry : hashMap.entrySet()) {
	            String file1 = entry.getKey();
	            Map<String, String> innerMap = entry.getValue();

	            System.out.println("filePaths1 = " + file1);
	            for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
	                String file2 = innerEntry.getKey();
	                String ignoreFile = innerEntry.getValue();

	                String resultPdf = "C:\\Users\\chetan.patel\\Documents\\docx\\New folder\\result\\result" + counter + ".pdf";
	                counter++;

	                System.out.println("    filePaths2 = " + file2);
	                System.out.println("    ignoreFile = " + ignoreFile);

	                // Call your docComparator and write the result to a unique PDF
	                new docComparator(file1, file2).withIgnore(ignoreFile).compare().writeTo(resultPdf);

	                System.out.println("Process is completed");
	            }
	        }

	        // Save the modified workbook
	        try (FileOutputStream fos = new FileOutputStream(filePath)) {
	            workbook.write(fos);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
