package at.ac.tuwien.digital_preservation.task3;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by nathaniel on 09.06.16.
 */
public class FetchImages {

    private static final Logger LOGGER = Logger.getLogger(FetchImages.class);
    private static final String PATH = "/tmp/digital.perservation/";
    private static final String BOXPLOT_NAME = "boxplot.png";
    private static final String CSV_NAME = "data.csv";
    private static final String BASE_URL = "http://xkcd.com";
    private static final String START_URL = "/1/";
    private static final int MAX_DEPTH = 100;

    private static ArrayList<XKCDStrip> strips;

    public static void main(String[] args) throws IOException, ImageProcessingException {

        // CHECK PATH TO SAVE STUFF TO
        File filePath = new File(PATH);

        if (!filePath.exists()) {

            filePath.mkdir();
        }

        LOGGER.info("START FETCHING...");

        strips = new ArrayList<>();

        // START PARSING
        parsePage(urlify(START_URL),1);

        LOGGER.info("DONE FETCHING.");

        // SAVE BOXPLOT
        saveBoxPlot(strips);

        // SAVE RESULTS
        saveCSV(strips);
    }

    private static void parsePage(String url, int currentDepth) throws IOException, ImageProcessingException {

        // GET DOCUMENT
        Document doc = Jsoup.connect(url).get();

        LOGGER.info("PARSING "+url);

        // GET NEXT PAGE URL
        Element nextLink = doc.select("a[rel=next]").first();

        if(nextLink != null){

            String nextPageURL = urlify(nextLink.attr("href"));

            // CREATE STRIP OBJECT
            Element image = doc.select("div[id=comic] > img").first();
            if(image != null){

                String imageURL = "http:"+image.attr("src");
                String fileExtension = "jpg";
                if(imageURL.contains(".png")){
                    fileExtension = "png";
                }
                int imageSize = (new URL(imageURL)).openConnection().getContentLength();
                String imageTitle = image.attr("title");
                String imageName = ((Integer)currentDepth).toString()+"."+fileExtension;

                XKCDStrip strip = new XKCDStrip();
                strip.stripUrl = url;
                strip.fileURL = imageURL;
                strip.fileSize = imageSize;
                strip.stripTitle = imageTitle;

                // SAVE FILE
                URL urlURL = new URL(imageURL);
                InputStream in = urlURL.openStream();

                OutputStream out = new BufferedOutputStream(new FileOutputStream(PATH+imageName));

                for (int b; (b = in.read()) != -1;) {
                    out.write(b);
                }
                out.close();
                in.close();

                File imageFile = new File(PATH+imageName);

                Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

                // obtain the Exif SubIFD directory
                ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                if(directory != null) {

                    strip.dateTaken = directory.getDateDigitized();
                }

                // ADD TO LIST
                strips.add(strip);
            }
            // GO TO NEXT PAGE UNTIL MAX DEPTH
            if(currentDepth <= MAX_DEPTH){

                parsePage(nextPageURL,++currentDepth);
            }
        }

    }

    private static String urlify(String path){

        return BASE_URL+path;
    }

    private static void saveBoxPlot(ArrayList<XKCDStrip> in) throws IOException {

        // SAVE BOXPLOT
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        ArrayList<Integer> fileSizes = new ArrayList<Integer>();

        for(XKCDStrip s: in){

            fileSizes.add((Integer)s.fileSize);
        }

        dataset.add(fileSizes,"XKCD Image Filesize","Filesize in Bytes");

        CategoryAxis xAxis = new CategoryAxis("Type");
        NumberAxis yAxis = new NumberAxis("Value");

        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
                "XKCD Comic File Sizes",
                new Font("SansSerif", Font.BOLD, 14),
                plot,
                true
        );

        ChartUtilities.saveChartAsPNG(new File(PATH + "boxplot.png"),chart,800,800);

        LOGGER.info("SAVED BOXPLOT.");
    }

    private static void saveCSV(ArrayList<XKCDStrip> in) throws FileNotFoundException {

        PrintWriter out = new PrintWriter(PATH+CSV_NAME);

        for(XKCDStrip e: in){

            out.println(e.toCSVLine());
            LOGGER.info(e.toCSVLine());
        }

        out.close();

        LOGGER.info("SAVED CSV.");
    }
}
