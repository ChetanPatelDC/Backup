package docCompare;

import static docCompare.Utilities.blockingExecutor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import docCompare.compareResultImplementation;
import docCompare.docComparator;
import docCompare.docComparator;
import docCompare.docComparator;

import docCompare.env.DefaultEnvironment;
import docCompare.env.Environment;

//import de.redsix.pdfcompare.CompareResultImpl;
//import de.redsix.pdfcompare.PdfComparator;



public class docComparator  <T extends compareResultImplementation> {
	
	  private static final Logger LOG = LoggerFactory.getLogger(docComparator.class);

	    private static final int TIMEOUT = 3;
	    private static final TimeUnit unit = TimeUnit.MINUTES;
	    public static final int MARKER_WIDTH = 20;

	    private Environment environment;
	    private Exclusions exclusions;
	    private  InputStreamSupplier expectedStreamSupplier;
	    private  InputStreamSupplier actualStreamSupplier;
	    private ExecutorService drawExecutor;
	    private ExecutorService parrallelDrawExecutor;
	    private ExecutorService diffExecutor;
	    private final T compareResult ;
	    private String expectedPassword = "";
	    private String actualPassword = "";
	    private boolean withIgnoreCalled = false;
	    private final ConcurrentLinkedQueue<Throwable> exceptionFromOtherThread = new ConcurrentLinkedQueue<>();
	
	
	    
	    
	    public static <T extends compareResultImplementation> docComparator base64(String expectedPdfBase64, String actualPdfBase64) {
	      return base64(expectedPdfBase64, actualPdfBase64, new compareResultImplementation());
	    }
	    
	   
	    public static <T extends compareResultImplementation> docComparator base64(String expectedPdfBase64, String actualPdfBase64, T compareResult) {  // Define base64 function with arguments expectedPdfBase64, actualPdfBase64 and compareResult	        docComparator docComparator = new docComparator<>(compareResult);  // Intialize a new pdfComparator object
	    	  docComparator docComparator = new docComparator<>(compareResult);  // Intialize a new pdfComparator object
	    	docComparator.expectedStreamSupplier = () -> new ByteArrayInputStream(Base64.getDecoder().decode(expectedPdfBase64)); // Convert the expected PDF base64 to array of bytes
	        docComparator.actualStreamSupplier = () -> new ByteArrayInputStream(Base64.getDecoder().decode(actualPdfBase64));   // Convert the actual PDF base64 to array of bytes
	        return docComparator; // Return pdfComparator instance  
	     }
	    
	    
	    
	    private docComparator(T compareResult) {    // Private constructor of PdfComparator class  
	        Objects.requireNonNull(compareResult, "compareResult is null");    // Check if compareResult is null or not
        this.compareResult = compareResult;  // Assign the value of compareResult to class variable compareResult
    }    // End the constructor
	    
	    
	  
	    public docComparator(String expectedPdfFilename, String actualPdfFilename) { // Defining function
	        this(expectedPdfFilename, actualPdfFilename, (T) new compareResultImplementation()); // Calling function
	    }
	    
	    
	    
	    
	    public docComparator(String expectedPdfFilename, String actualPdfFilename, T compareResult) { // PdfComparator class to compare pdf files
	        this(compareResult);   // Call comparator class
	        Objects.requireNonNull(expectedPdfFilename, "expectedPdfFilename is null");// Check if expected filename is null or not
	        Objects.requireNonNull(actualPdfFilename, "actualPdfFilename is null"); // Check if actual filename is null or not
	        if (!expectedPdfFilename.equals(actualPdfFilename)) { // Check if expected filename is not equal to actual filename or not
	            this.expectedStreamSupplier = () -> Files.newInputStream(Paths.get(expectedPdfFilename));   // Get input stream using expected filename
	            this.actualStreamSupplier = () -> Files.newInputStream(Paths.get(actualPdfFilename));  // Get input stream using actual filename
	        } 
	    }
	    
	    
	    

	    public docComparator(final Path expectedPath, final Path actualPath) { // Constructor of PdfComparator class
	        this(expectedPath, actualPath, (T) new compareResultImplementation());    // Initialize PdfComparator object
	    }
	    
	    public docComparator(final Path expectedPath, final Path actualPath, final T compareResult) {
	        this(compareResult);
	        Objects.requireNonNull(expectedPath, "expectedPath is null");// Checking if expectedPath is null or not and throw exception if null
	        Objects.requireNonNull(actualPath, "actualPath is null");// Checking if actualPath is null or not and throw exception if null
	        if (!expectedPath.equals(actualPath)) { // Check if expected path is equal to actual path
	            this.expectedStreamSupplier = () -> Files.newInputStream(expectedPath);// Assigning new input stream to expectedStreamSupplier
	            this.actualStreamSupplier = () -> Files.newInputStream(actualPath);  // Assigning new input stream to actualStreamSupplier
	        }//added
	    }
	        
	        
	        public docComparator(final File expectedFile, final File actualFile) { // Constructor of PdfComparator class
	            this(expectedFile, actualFile, (T) new compareResultImplementation());   // Initialize the object of PdfComparator
	        }  
	        
	        public docComparator(final File expectedFile, final File actualFile, final T compareResult) {  //Same as Above
	           this(compareResult);
	            Objects.requireNonNull(expectedFile, "expectedFile is null");
	            Objects.requireNonNull(actualFile, "actualFile is null");
	            if (!expectedFile.equals(actualFile)) {
	                this.expectedStreamSupplier = () -> new FileInputStream(expectedFile);
	               this.actualStreamSupplier = () -> new FileInputStream(actualFile);
            }
	        }
	        
	        
	        public docComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS) { // Constructor with two InputStream parameters
	            this(expectedPdfIS, actualPdfIS, (T) new compareResultImplementation());   // Call the constructor with three parameters
	        }
	        
	        
	        public docComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS, final T compareResult) { // Initialize PdfComparator
	            this(compareResult); // Call the constructor
	            Objects.requireNonNull(expectedPdfIS, "expectedPdfIS is null");  // Check if expectedPdfIS is null
	            Objects.requireNonNull(actualPdfIS, "actualPdfIS is null");  // Check if actualPdfIS is null
	            if (!expectedPdfIS.equals(actualPdfIS)) {   // check if expectedPdfIS and actualPdfIS are equal or not
	                this.expectedStreamSupplier = () -> expectedPdfIS;  // assign value to expectedStreamSupplier
	                this.actualStreamSupplier = () -> actualPdfIS;   // assign value to actualStreamSupplier
	            }
	        }
	        
	        
	        @Deprecated   // Declare a class or member deprecated.
	     public void setEnvironment(Environment environment) { // Defining function setEnvironment to set environment for the application
	           withEnvironment(environment);  // Set the environment for the application
	        }        
	        
	        
	        
	        public docComparator<T> withEnvironment(Environment environment) { // Defining method withEnvironment
	            if (withIgnoreCalled) { //Check if withIgnoreCalled is true
	                throw new IllegalStateException("withEnvironment(...) must be called before any withIgnore(...) methods are called.");// Throw error
	            }
	            this.environment = environment;   // Assign environment to environment variable of class
	            return this;    // Return this
	        } 
	        
	        
	        
	    // With ignore part

		public docComparator<T> withIgnore(final String ignoreFilename) { // public function withIgnore
	        Objects.requireNonNull(ignoreFilename, "ignoreFilename is null");// Objects.requireNonNull() will throw NullPointerException if the parameter is null
	        withIgnoreCalled = true;  // Assign true to withIgnoreCalled
	        getExclusions().readExclusions(ignoreFilename);  // Return the reference of the current object
	        return this;
	    } 
	    
	    
	    public docComparator<T> withIgnore(final File ignoreFile) {  //same as above, it take file as a parameter
	        Objects.requireNonNull(ignoreFile, "ignoreFile is null");
	        withIgnoreCalled = true;
	        getExclusions().readExclusions(ignoreFile);
	        return this;
	    }
	    
	    public docComparator<T> withIgnore(final Path ignorePath) {// Declaring withIgnore() function
	        Objects.requireNonNull(ignorePath, "ignorePath is null");// Check if ignorePath is null or not
	        withIgnoreCalled = true;// Set the value of withIgnoreCalled variable to true
	        getExclusions().readExclusions(ignorePath);  // Call readExclusions
	        return this;
	    }
	    
	    
	    public docComparator<T> withIgnore(final InputStream ignoreIS) { // function withIgnore defined which takes InputStream as parameter
	        Objects.requireNonNull(ignoreIS, "ignoreIS is null");  // check if ignoreIS is null or not
	        withIgnoreCalled = true; // Assign value true to withIgnoreCalled variable
	        getExclusions().readExclusions(ignoreIS);  // call function readExclusions() on object getExclusions()
	        return this;    // return the object
	       
	    }
	    
	    
	    public docComparator<T> withIgnore(final PageArea exclusion) {// function withIgnore defined which takes PageArea as parameter
	        Objects.requireNonNull(exclusion, "exclusion is null");// check if exclusion is null or not
	        withIgnoreCalled = true;// Assign value true to withIgnoreCalled variable
	        getExclusions().add(exclusion);// call function add() on object getExclusions()
	        return this;
	    }
	    
	    @Deprecated
	    public docComparator<T> with(final PageArea exclusion) {// defines a method called "with" within a class called "PdfComparator". It takes in a parameter called "exclusion" which is of type PageArea.
	        return withIgnore(exclusion);// calls the method withIgnore and pass the exclusion area as parameter and then return the object returned by withIgnore method
	    }
	    
	    
	    
	    public docComparator<T> withExpectedPassword(final String password) {// defines a method called "withExpectedPassword" within a class called "PdfComparator". It takes in a parameter called "password" which is a string representing the expected password.
	        Objects.requireNonNull(password, "password is null");// calls the method Objects.requireNonNull(password, "password is null"). This method throws a NullPointerException if the parameter is null, which means it checks whether the passed parameter is null or not.
	        expectedPassword = password;//This line assigns the value of the parameter "password" to the variable "expectedPassword" within the class.
	       return this;
	    }
	    
	    public docComparator<T> withActualPassword(final String password) {// defines a method called "withActualPassword" within a class called "PdfComparator". It takes in a parameter called "password" which is a string representing the actual password.
	        Objects.requireNonNull(password, "password is null");// calls the method Objects.requireNonNull(password, "password is null"). This method throws a NullPointerException if the parameter is null, which means it checks whether the passed parameter is null or not.
	        actualPassword = password;// assigns the value of the parameter "password" to the variable "actualPassword" within the class.
	       return this;  
	    }
	    
	    
	   //Exclusion part	    
	    
	    
	    private Exclusions getExclusions() {// a private method called "getExclusions" within the class "PdfComparator". This method returns an object of class "Exclusions".
	        if (exclusions == null) {   
	            exclusions = new Exclusions(getEnvironment());//This line creates a new object of class "Exclusions" and assigns it to the variable "exclusions" and passes the environment object to the constructor of Exclusions class.
	        }
	        return exclusions;//This line returns the object of class "Exclusions"
	     }//added line 355
	    
	    
	    private Environment getEnvironment() {// a private method called "getEnvironment" within the class "PdfComparator". This method returns an object of class "Environment".
	        if (environment == null) {
	            environment = DefaultEnvironment.create();// creates a new object of class "Environment" and assigns it to the variable "environment"
	        }
	        return environment;
	         
	    }//Both methods are getter methods which are used to get the Exclusions and Environment objects, if they are not created yet, it creates new objects and return them.

	    
	    private void buildEnvironment() {//defines a private method called "buildEnvironment" within the class "PdfComparator". This method is used to build the environment for comparison.
	        compareResult.setEnvironment(getEnvironment()); //sets the environment object returned by the method getEnvironment to the compareResult object

	        drawExecutor = blockingExecutor("Draw", 1, 50, environment);// creates a new object of class "blockingExecutor" and assigns it to the variable "drawExecutor" with name "Draw" and the number of threads used to execute the task is 1 and the maximum queue size is 50, and the environment object is passed to the constructor.
	        parrallelDrawExecutor = blockingExecutor("ParallelDraw", 2, 4, environment);//creates a new object of class "blockingExecutor" and assigns it to the variable "parallelDrawExecutor" with name "ParallelDraw" and the number of threads used to execute the task is 2 and the maximum queue size is 4, and the environment object is passed to the constructor.
	        diffExecutor = blockingExecutor("Diff", 1, 2, environment);     //creates a new object of class "blockingExecutor" and assigns it to the variable "diffExecutor" with name "Diff" and the number of threads used to execute the task is 1 and the maximum queue size is 2, and the environment object is passed to the constructor.
	    }
	    
	    
	    
	    
	    
	    
	    public T compare() throws IOException, RenderingException {
	        try {
	            if (expectedStreamSupplier == null || actualStreamSupplier == null) {
	            	System.out.println("There is no file");  // edited
	                return compareResult; //This line returns the compareResult object if either of the stream supplier is null
	            }
	           buildEnvironment();  //calls the method buildEnvironment() to set up the environment for the comparison
	            try (final InputStream expectedStream = expectedStreamSupplier.get()) {    //opens a new InputStream object using the expectedStreamSupplier and assigns it to "expectedStream", and also uses try-with-resources block to automatically close the stream when done.
	                try (final InputStream actualStream = actualStreamSupplier.get()) {   //opens a new InputStream object using the actualStreamSupplier and assigns it to "actualStream", and also uses try-with-resources block to automatically close the stream when done.
	                    try (PDDocument expectedDocument = PDDocument
	                          .load(expectedStream, expectedPassword, Utilities.getMemorySettings(environment.getDocumentCacheSize()))) {// opens a new PDDocument object using the expectedStream, expectedPassword, and memory settings and assigns it to "expectedDocument" and uses try-with-resources block to automatically close the document when done.
	                        try (PDDocument actualDocument = PDDocument
	                                .load(actualStream, actualPassword, Utilities.getMemorySettings(environment.getDocumentCacheSize()))) {
	                            compare(expectedDocument, actualDocument);
	                        }
	                    }
	                } catch (NoSuchFileException ex) { ex.printStackTrace(); //edited
	                LOG.warn("No files found to compare. Tried Expected: '{}' and Actual: '{}'", ex.getFile(), ex.getFile());  //edited
	                    addSingleDocumentToResult(expectedStream, environment.getActualColor().getRGB());
	                   compareResult.expectedOnly();
	                }
	            } catch (NoSuchFileException ex) {ex.printStackTrace();  // edited
	           LOG.warn("No files found to compare. Tried Expected: '{}' and Actual: '{}'", ex.getFile(), ex.getFile());  //edited
	                try (final InputStream actualStream = actualStreamSupplier.get()) {
	                    addSingleDocumentToResult(actualStream, environment.getExpectedColor().getRGB());
	                   compareResult.actualOnly();//It will add a single document to the result object accordingly if any expected or actual object found to be null.
	               } catch (NoSuchFileException innerEx) {
	                   LOG.warn("No files found to compare. Tried Expected: '{}' and Actual: '{}'", ex.getFile(), innerEx.getFile());
	                    compareResult.noPagesFound();// Log.warn-> warning should pop out that no file found if both expected and actual document found to be null.
	              }
	            }
	        } finally {
	            compareResult.done();//Finally, the method calls the done() method on the comparison result object, and returns the comparison result object.
	        } 
	        return compareResult;
	    }
	    
	    
	    private void compare(final PDDocument expectedDocument, final PDDocument actualDocument) throws IOException {
	        expectedDocument.setResourceCache(new ResourceCacheWithLimitedImages(environment));
	        PDFRenderer expectedPdfRenderer = new PDFRenderer(expectedDocument);

	        actualDocument.setResourceCache(new ResourceCacheWithLimitedImages(environment));
	        PDFRenderer actualPdfRenderer = new PDFRenderer(actualDocument);
	// iterates through the pages of the two PDF documents and compares them.
	  //The number of pages to be compared is determined by the minimum number of pages between the expected and actual documents, using the Math.min() method.
	        final int minPageCount = Math.min(expectedDocument.getNumberOfPages(), actualDocument.getNumberOfPages());
	        CountDownLatch latch = new CountDownLatch(minPageCount);
	        //A CountDownLatch object is created with the number of pages to be compared as its initial count.
	        for (int pageIndex = 0; pageIndex < minPageCount; pageIndex++) {
	            drawImage(latch, pageIndex, expectedDocument, actualDocument, expectedPdfRenderer, actualPdfRenderer);
	       
	       }//For each page, the code calls a method named drawImage, passing in the CountDownLatch, the current page index, and the expected and actual documents, along with their PDFRenderers. 
	        //This method is probably used to draw images of the pages, in order to compare them. 
	        //The latch is used to wait for all the pages to be compared before continuing with the rest of the code.
	        Utilities.await(latch, "FullCompare", environment);
	        Utilities.shutdownAndAwaitTermination(drawExecutor, "Draw");
	        Utilities.shutdownAndAwaitTermination(parrallelDrawExecutor, "Parallel Draw");
	        Utilities.shutdownAndAwaitTermination(diffExecutor, "Diff");
	        // it appears to be shutting down and awaiting termination of several executors (drawExecutor, parrallelDrawExecutor, and diffExecutor) using the Utilities class method shutdownAndAwaitTermination.
	        if (expectedDocument.getNumberOfPages() > minPageCount) {
	            addExtraPages(expectedDocument, expectedPdfRenderer, minPageCount, environment.getActualColor().getRGB(), true);
	        } else if (actualDocument.getNumberOfPages() > minPageCount) {
	            addExtraPages(actualDocument, actualPdfRenderer, minPageCount, environment.getExpectedColor().getRGB(), false);
	        } //It is also adding extra pages to PDF documents (expectedDocument and actualDocument) using the addExtraPages method, if the number of pages in the documents is greater than a specified minimum page count.
	        if (!exceptionFromOtherThread.isEmpty()) {
	            RenderingException ex = new RenderingException("Exceptions were caught during rendering or diffing");
	            exceptionFromOtherThread.forEach(ex::addSuppressed);
	            throw ex;  //Finally, it checks for any exceptions caught from other threads and throws a new RenderingException with those suppressed exceptions if any were caught.
	        }
	    }

	    private void drawImage(final CountDownLatch latch, final int pageIndex, // calling drawimage method and passing the parameter as count latch pageindex
	            final PDDocument expectedDocument, final PDDocument actualDocument, // declaring the expected document and actual document as PD document
	           final PDFRenderer expectedPdfRenderer, final PDFRenderer actualPdfRenderer) { // takes two pdf renderer object actualpdfrenderer and expectedpdfrenderer
	       drawExecutor.execute(() -> { // the execute method from the drawExecutor object to submit a task for execution
	           try {
	               LOG.trace("Drawing page {}", pageIndex);   //the trace method from the LOG object to log a message indicating that the code is currently drawing a specific page. The {} placeholder is replaced with the value of the pageIndex variable.
	               final Future<ImageWithDimension> expectedImageFuture = parrallelDrawExecutor  //This line creates a Future object, expectedImageFuture,		                                                                    
	                      .submit(() -> renderPageAsImage(expectedDocument, expectedPdfRenderer, pageIndex, environment));  // using the submit method from the parrallelDrawExecutor object. The task that is passed to the submit method is a lambda expression that calls the renderPageAsImage method, passing in the expectedDocument, expectedPdfRenderer, pageIndex and environment as arguments. This will render a specific page from the expectedDocument as an image.
	               final Future<ImageWithDimension> actualImageFuture = parrallelDrawExecutor  //same as above, This will render a specific page from the actualDocument as an image.
	                      .submit(() -> renderPageAsImage(actualDocument, actualPdfRenderer, pageIndex, environment));      //This line gets the image from the expectedImageFuture object by calling the getImage method, passing in the expectedImageFuture, pageIndex, and a string "expected document" as arguments.
	              final ImageWithDimension expectedImage = getImage(expectedImageFuture, pageIndex, "expected document");   // The method returns an ImageWithDimension object, which is stored in the expectedImage variable.
	                final ImageWithDimension actualImage = getImage(actualImageFuture, pageIndex, "actual document");
	               final DiffImage diffImage = new DiffImage(expectedImage, actualImage, pageIndex, environment, getExclusions(), compareResult);
	              LOG.trace("Enqueueing page {}.", pageIndex);  // object to log a message indicating that the code is currently enqueuing a specific page.//for debugging and tracking the progress of the code execution.
	               diffExecutor.execute(() -> {
	                    LOG.trace("Diffing page {}", diffImage);  // diffImage is an instance of DiffImage class and contains the image data and other information to perform the difference.
	                  try {
	                        diffImage.diffImages();  //This line calls the diffImages() method of the diffImage object to perform the image difference.
	                   } catch (Throwable t) { //This line catches any exceptions that may have been thrown in the try block.
	                       addErrorPage(pageIndex, "An error occurred, while diffing this page", t);  //If an exception is caught, this line calls the addErrorPage method, passing in the pageIndex, a string "An error occurred, while diffing this page" and the caught exception (t) as arguments. This method is likely used to log or report the error and the page on which it occurred.
	                   }
	                    LOG.trace("DONE Diffing page {}", diffImage); //This line uses the trace method from the LOG object to log a message indicating that the difference operation on a specific page is done. The {} placeholder is replaced with the value of the diffImage variable. This log message is useful for debugging and tracking the progress of the code execution.
	               });
	                LOG.trace("DONE drawing page {}", pageIndex);
	           } catch (Throwable t) {
	                addErrorPage(pageIndex, "An error occurred, while rendering this page", t);
	            } finally {
	                latch.countDown();
	            }
	       });
	   }

	    private void addErrorPage(int pageIndex, String message, Throwable t) {
	        LOG.error(message, t);
	        exceptionFromOtherThread.add(t);
	        StacktraceImage stacktraceImage = new StacktraceImage(message, t, environment);
	        ImageWithDimension errorImage = stacktraceImage.getImage();
	        compareResult.addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, stacktraceImage.getBlankImage(), errorImage, errorImage);
	    }

	    private ImageWithDimension getImage(final Future<ImageWithDimension> imageFuture, final int pageIndex, final String type) {
	        try {
	            return imageFuture.get(TIMEOUT, unit);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            throw new RenderingException("Waiting for Future was interrupted while rendering page " + (pageIndex + 1) + " of " + type, e);
	        } catch (TimeoutException e) {
	            String msg = String.format("Waiting for Future timed out after %d %s while rendering page %d of %s", TIMEOUT, unit, pageIndex + 1, type);
	            throw new RenderingException(msg, e);
	        } catch (ExecutionException e) {
	            throw new RenderingException("Error while rendering page " + (pageIndex + 1) + " of " + type, e);
	        }
	    }

	    private void addSingleDocumentToResult(InputStream expectedPdfIS, int markerColor) throws IOException {
	        try (PDDocument expectedDocument = PDDocument.load(expectedPdfIS)) {
	            PDFRenderer expectedPdfRenderer = new PDFRenderer(expectedDocument);
	            addExtraPages(expectedDocument, expectedPdfRenderer, 0, markerColor, true);
	        }
	    }

	    private void addExtraPages(final PDDocument document, final PDFRenderer pdfRenderer, final int minPageCount,
	            final int color, final boolean expected) throws IOException {
	        for (int pageIndex = minPageCount; pageIndex < document.getNumberOfPages(); pageIndex++) {
	            ImageWithDimension image = renderPageAsImage(document, pdfRenderer, pageIndex, environment);
	            final DataBuffer dataBuffer = image.bufferedImage.getRaster().getDataBuffer();
	            for (int i = 0; i < image.bufferedImage.getWidth() * MARKER_WIDTH; i++) {
	                dataBuffer.setElem(i, color);
	            }
	            for (int i = 0; i < image.bufferedImage.getHeight(); i++) {
	                for (int j = 0; j < MARKER_WIDTH; j++) {
	                    dataBuffer.setElem(i * image.bufferedImage.getWidth() + j, color);
	                }
	            }
	            if (expected) {
	                compareResult.addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, image, blank(image), image);
	            } else {
	                compareResult.addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, blank(image), image, image);
	            }
	        }
	    }

	    private static ImageWithDimension blank(final ImageWithDimension image) {
	        return new ImageWithDimension(
	                new BufferedImage(image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), image.bufferedImage.getType()),
	                image.width, image.height);
	    }

	    public static ImageWithDimension renderPageAsImage(final PDDocument document, final PDFRenderer expectedPdfRenderer, final int pageIndex,
	            Environment environment) throws IOException {
	        final BufferedImage bufferedImage = expectedPdfRenderer.renderImageWithDPI(pageIndex, environment.getDPI());
	        final PDPage page = document.getPage(pageIndex);
	        final PDRectangle mediaBox = page.getMediaBox();
	        if (page.getRotation() == 90 || page.getRotation() == 270) {
	            return new ImageWithDimension(bufferedImage, mediaBox.getHeight(), mediaBox.getWidth());
	        } else {
	            return new ImageWithDimension(bufferedImage, mediaBox.getWidth(), mediaBox.getHeight());
	        }
	    }

	    public T getResult() {
	        return compareResult;
	    }
	    
	    
	   
	   
	    
	    @FunctionalInterface
	    private interface InputStreamSupplier {

	        InputStream get() throws IOException;
	    }





		public static int docComparator(String file1, String file2, String ignoreFile) {
			// TODO Auto-generated method stub
			return 0;
		}

}
