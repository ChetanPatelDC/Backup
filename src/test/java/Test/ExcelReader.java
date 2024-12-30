package Test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import docCompare.compareResultImplementation;
import docCompare.docComparator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExcelReader {
	
	 public static final String TIME_STAMP = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss").format(new Date());
	 

    public static void main(String[] args) {
        String filePath = "C:\\Users\\chetan.patel\\Documents\\docx\\Book123.xlsx";
        String sheetName = "Sheet1";
        String reportFilePath = "C:\\Users\\chetan.patel\\Documents\\docx\\pdf_reports\\PDF_report"+TIME_STAMP+".csv";

        try (Workbook workbook = new XSSFWorkbook(filePath);
             FileWriter writer = new FileWriter(reportFilePath)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Map<String, String>> hashMap = new LinkedHashMap<>();
            Map<String, String> reportData = new HashMap<>(); // Store report for each PDF file

            int rowNumber = 0; // Add a row counter for debugging
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNumber++; // Increment row counter

                Cell file1Cell = row.getCell(0);
                Cell file2Cell = row.getCell(1);
                Cell ignoreCell = row.getCell(2);

                String file1 = dataFormatter.formatCellValue(file1Cell);
                String file2 = dataFormatter.formatCellValue(file2Cell);
                String ignoreFile = dataFormatter.formatCellValue(ignoreCell);

                // Debugging: Print row details
                System.out.println("Row " + rowNumber + ": file1 = " + file1 + ", file2 = " + file2);

                if (file1.isEmpty() || file2.isEmpty()) {
                    System.out.println("Skipping row " + rowNumber + " due to missing file1 or file2.");
                    continue; // Skip this row if either file1 or file2 is empty
                }

                if (!hashMap.containsKey(file1)) {
                    hashMap.put(file1, new HashMap<>());
                }
                Map<String, String> innerMap = hashMap.get(file1);
                innerMap.put(file2, ignoreFile);
            }

            String line1 = "File 1" + "," + "File 2" + "," + " Result Pdf Name" + "," + "Difference Found" + "," + "\n";
            writer.write(line1);

            int counter = 1;
            int count = 1;
            for (Map.Entry<String, Map<String, String>> entry : hashMap.entrySet()) {
                String file1 = entry.getKey();
                Map<String, String> innerMap = entry.getValue();

                System.out.println("file1 = " + file1);

                for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
                    String file2 = innerEntry.getKey();
                    String ignoreFile = innerEntry.getValue();

                    String resultPdf = "C:\\Users\\chetan.patel\\Documents\\docx\\New folder\\result\\result" + counter + ".pdf";
                    counter++;
                    String result = "result" + count + ".pdf";
                    count++;

                    System.out.println("    file2 = " + file2);
                    System.out.println("    ignoreFile = " + ignoreFile);

                    // Use a single call for the comparison and PDF writing
                    compareResultImplementation comparator = new docComparator(file1, file2).withIgnore(ignoreFile).compare();
                    boolean differencesFound = comparator.isEqual();
                    comparator.writeTo(resultPdf); // Write to PDF

                    System.out.println("Process is completed for " + file1 + " and " + file2);

                    // Generate the report
                    String reportLine = file1 + "," + file2 + "," + result + "," + (differencesFound ? "No" : "Yes") + "," + "\n";
                    writer.write(reportLine);

                    // Store the report for the PDF file
                    String pdfFileName = file2.substring(file2.lastIndexOf("\\") + 1); // Extract the PDF file name
                    reportData.put(pdfFileName, differencesFound ? "No" : "Yes");
                }
            }

            // Print the report data
            for (Map.Entry<String, String> entry : reportData.entrySet()) {
             //   System.out.println("PDF File: " + entry.getKey() + ", Differences Found: " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
