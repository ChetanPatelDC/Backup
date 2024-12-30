package docCompare;

public class DOC_Compare {
	
	
	
	public static void main(String[] args)throws Exception {
		String file1 = "D:\\New Data\\PDF\\pdf_Compare\\Bank-Statement-Template-1-TemplateLab.pdf";

		String file2 = "D:\\New Data\\PDF\\pdf_Compare\\Chase-Bank-Statement-TemplateLab.com_.pdf";

		String resultpdf = "D:\\New Data\\PDF\\Result\\result_1";

		String ignoreFile="C:\\Users\\chetan.patel\\eclipse-workspace\\docComparator\\src\\test\\java\\docCompare\\ignore.conf";

	
		// .withIgnore(ignoreFile)
		
	//	new docComparator(file1,file2).compare().writeTo(resultpdf);

	//  new docComparator(file1, file2).withIgnore(ignoreFile).compare().writeTo(resultpdf);
	 
	 new docComparator(file1, file2, new CompareResultWithPageOverflow()).withIgnore(ignoreFile).compare().writeTo(resultpdf);
		

		System.out.println("Process Completed");

		 

		}

		 

}
