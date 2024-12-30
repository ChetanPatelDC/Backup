package docCompare;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import docCompare.env.Environment;

public class compareResultImplementation implements ResultCollector, CompareResult{
	  private static final Logger LOG = LoggerFactory.getLogger(compareResultImplementation.class);
	    protected Environment environment;
	    protected final Map<Integer, ImageWithDimension> diffImages = new TreeMap<>();
	    protected boolean isEqual = true;
	    protected boolean hasDifferenceInExclusion = false;
	    private boolean expectedOnly;
	    private boolean actualOnly;
	    private final Collection<PageArea> diffAreas = new ArrayList<>();
	    private final Map<Integer, Double> diffPercentages = new TreeMap<>();
	    private int pages = 0;

	    @Override
	    public boolean writeTo(String filename) {
	        return writeTo(doc -> doc.save(filename + ".pdf"));
	    }

	    @Override
	    public boolean writeTo(final OutputStream outputStream) {
	        Objects.requireNonNull(outputStream, "OutputStream must not be null");
	        final boolean result = writeTo(doc -> doc.save(outputStream));
	        silentlyCloseOutputStream(outputStream);
	        return result;
	    }

	    private boolean writeTo(ThrowingConsumer<PDDocument, IOException> saver) {
	        if (hasImages()) {
	            try (PDDocument document = new PDDocument()) {
	                addImagesToDocument(document);
	                saver.accept(document);
	            } catch (IOException e) {
	                throw new RuntimeException(e);
	            }
	        }
	        return isEqual;
	    }

	    private void silentlyCloseOutputStream(final OutputStream outputStream) {
	        try {
	            outputStream.close();
	        } catch (IOException e) {
	            LOG.info("Could not close OutputStream", e);
	        }
	    }

	    /**
	     * checks, whether this CompareResult has stored images.
	     *
	     * @return true, when images are stored in this CompareResult
	     */
	    protected synchronized boolean hasImages() {
	        return !diffImages.isEmpty();
	    }

	    protected synchronized void addImagesToDocument(final PDDocument document) throws IOException {
	        addImagesToDocument(document, diffImages);
	    }

	    protected synchronized void addImagesToDocument(final PDDocument document, final Map<Integer, ImageWithDimension> images)
	            throws IOException {
	        final Iterator<Entry<Integer, ImageWithDimension>> iterator = images.entrySet().iterator();
	        while (iterator.hasNext()) {
	            final Entry<Integer, ImageWithDimension> entry = iterator.next();
	            if (!keepImages()) {
	                iterator.remove();
	            }
	            addPageToDocument(document, entry.getValue());
	        }
	    }

	    protected void addPageToDocument(final PDDocument document, final ImageWithDimension image) throws IOException {
	        PDPage page = new PDPage(new PDRectangle(image.width, image.height));
	        document.addPage(page);
	        final PDImageXObject imageXObject = LosslessFactory.createFromImage(document, image.bufferedImage);
	        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
	            contentStream.drawImage(imageXObject, 0, 0, image.width, image.height);
	        }
	    }

	    protected boolean keepImages() {
	        return false;
	    }

	    @Override
	    public synchronized void addPage(final PageDiffCalculator diffCalculator, final int pageIndex,
	            final ImageWithDimension expectedImage, final ImageWithDimension actualImage, final ImageWithDimension diffImage) {
	        Objects.requireNonNull(expectedImage, "expectedImage is null");
	        Objects.requireNonNull(actualImage, "actualImage is null");
	        Objects.requireNonNull(diffImage, "diffImage is null");
	        this.hasDifferenceInExclusion |= diffCalculator.differencesFoundInExclusion();
	        diffPercentages.put(pageIndex, diffCalculator.getDifferenceInPercent());
	        if (diffCalculator.differencesFound()) {
	            isEqual = false;
	            diffAreas.add(diffCalculator.getDiffArea());
	            diffImages.put(pageIndex, diffImage);
	            pages++;
	        } else if (environment.addEqualPagesToResult()) {
	            diffImages.put(pageIndex, diffImage);
	            pages++;
	        }
	    }

	    @Override
	    public void noPagesFound() {
	        isEqual = false;
	    }

	    @Override
	    public boolean isEqual() {
	        return isEqual;
	    }

	    @Override
	    public boolean isNotEqual() {
	        return !isEqual;
	    }

	    @Override
	    public boolean hasDifferenceInExclusion() {
	        return hasDifferenceInExclusion;
	    }

	    @Override
	    public boolean hasOnlyExpected() {
	        return expectedOnly;
	    }

	    @Override
	    public boolean hasOnlyActual() {
	        return actualOnly;
	    }

	    @Override
	    public boolean hasOnlyOneDoc() {
	        return expectedOnly || actualOnly;
	    }

	    @Override
	    public int getNumberOfPages() {
	        return pages;
	    }

	    @Override
	    public Collection<PageArea> getDifferences() {
	        return diffAreas;
	    }

	    @Override
	    public String getDifferencesJson() {
	        return PageArea.asJsonWithExclusion(getDifferences());
	    }

	    @Override
	    public Collection<Integer> getPagesWithDifferences() {
	        return diffAreas.stream().map(a -> a.page).collect(Collectors.toList());
	    }

	    @Override
	    public Map<Integer, Double> getPageDiffsInPercent() {
	        return diffPercentages;
	    }

	    public void expectedOnly() {
	        this.expectedOnly = true;
	    }

	    public void actualOnly() {
	        this.actualOnly = true;
	    }

	    public void setEnvironment(Environment environment) {
	        this.environment = environment;
	    }

	
	}
