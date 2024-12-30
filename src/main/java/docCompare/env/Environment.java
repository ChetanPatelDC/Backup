package docCompare.env;

import java.awt.Color;
import java.nio.file.Path;
import java.awt.*;

public interface Environment {
	
	
	 Path getTempDirectory();

	    int getNrOfImagesToCache();

	    int getMergeCacheSize();

	    int getSwapCacheSize();

	    int getDocumentCacheSize();

	    int getMaxImageSize();

	    int getOverallTimeout();

	    boolean useParallelProcessing();

	    double getAllowedDiffInPercent();

	    Color getExpectedColor();

	    Color getActualColor();

	    int getDPI();

	    boolean addEqualPagesToResult();

	    boolean failOnMissingIgnoreFile();
	}



