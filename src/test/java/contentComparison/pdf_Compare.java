package contentComparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.sequence.EditScript;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class pdf_Compare {
		
	  public static void main(String[] args) {
	        try {
	            File pdfFile1 = new File("D:\\New_Data\\PDF\\pdf_Compare\\Test1.pdf");
	            File pdfFile2 = new File("D:\\New_Data\\PDF\\pdf_Compare\\Test2.pdf");

	            List<Integer> pagesToIgnore = new ArrayList<>(); 
	            pagesToIgnore.add(1);   // Ignore page 2
	             // Ignore page 4// Specify the page numbers to ignore
	            String text1 = extractTextFromPdf(pdfFile1, pagesToIgnore);
	            String text2 = extractTextFromPdf(pdfFile2, pagesToIgnore);

	            List<String> text1Lines = List.of(text1.split("\n"));
	            List<String> text2Lines = List.of(text2.split("\n"));

	            Patch<String> patch = DiffUtils.diff(text1Lines, text2Lines);

	            if (patch.getDeltas().isEmpty()) {
	                System.out.println("No difference found.");
	            } else {
	                System.out.println("Differences found:");
	                for (Delta<String> delta : patch.getDeltas()) {
	                    System.out.println(delta);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public static String extractTextFromPdf(File pdfFile, List<Integer> pagesToIgnore) throws IOException {
	        try (PDDocument document = PDDocument.load(pdfFile)) {
	            PDFTextStripper stripper = new PDFTextStripper();

	            int pageCount = document.getNumberOfPages();
	            StringBuilder extractedText = new StringBuilder();

	            for (int i = 0; i < pageCount; i++) {
	                if (!pagesToIgnore.contains(i + 1)) { // Pages are 1-indexed
	                    stripper.setStartPage(i + 1);
	                    stripper.setEndPage(i + 1);
	                    extractedText.append(stripper.getText(document));
	                }
	            }

	            return extractedText.toString();
	        }
	    }
	}