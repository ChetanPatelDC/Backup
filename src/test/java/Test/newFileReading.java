package Test;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import docCompare.CompareResult;
import docCompare.compareResultImplementation;
import docCompare.docComparator;

public class newFileReading {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\chetan.patel\\Documents\\docx\\Book123.xlsx";
        String sheetName = "Sheet1";
        String reportFilePath = "C:\\Users\\chetan.patel\\Documents\\docx\\PDF_report.csv";

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
             FileWriter writer = new FileWriter(reportFilePath)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Map<String, String>> hashMap = new LinkedHashMap<>();
            Map<String, String> reportData = new HashMap<>(); // Store report for each PDF file

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell file1Cell = row.getCell(0);
                Cell file2Cell = row.getCell(1);
                Cell ignoreCell = row.getCell(2);

                if (file1Cell == null || file2Cell == null || ignoreCell == null) {
                    System.out.println("Skipping row due to missing cell data");
                    continue;
                }

                String file1 = dataFormatter.formatCellValue(file1Cell);
                String file2 = dataFormatter.formatCellValue(file2Cell);
                String ignoreFile = dataFormatter.formatCellValue(ignoreCell);

                if (file1.isEmpty() || file2.isEmpty() || ignoreFile.isEmpty()) {
                    System.out.println("Skipping row due to empty cell data");
                    continue;
                }

                if (!hashMap.containsKey(file1)) {
                    hashMap.put(file1, new HashMap<>());
                }
                Map<String, String> innerMap = hashMap.get(file1);
                innerMap.put(file2, ignoreFile);
            }

            String line1 = "File 1,File 2,Result Pdf Name,Difference Found\n";
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

                    // Call your pdfComparator method from pdfCompare class
                    compareResultImplementation comparator = new docComparator(file1, file2).withIgnore(ignoreFile).compare();
                    boolean differencesFound = ((CompareResult) comparator).isEqual();

                    // Write differences to the result PDF
                    comparator.writeTo(resultPdf);
                    System.out.println("Process is completed");

                    // Generate the report
                    String reportLine = file1 + "," + file2 + "," + result + "," + (differencesFound ? "Yes" : "No") + "\n";
                    writer.write(reportLine);

                    // Store the report for the PDF file
                    String pdfFileName = file2.substring(file2.lastIndexOf("\\") + 1); // Extract the PDF file name
                    reportData.put(pdfFileName, differencesFound ? "No" : "Yes");

                    // Print the difference coordinates if any differences are found
                    if (differencesFound) {
                        System.out.println("Differences found in " + file1 + " and " + file2);
                        // If getDifferences method exists, use it to print the coordinates
                        // System.out.println("Difference coordinates: " + comparator.getDifferences());
                    } else {
                        System.out.println("No differences found in " + file1 + " and " + file2);
                    }
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
