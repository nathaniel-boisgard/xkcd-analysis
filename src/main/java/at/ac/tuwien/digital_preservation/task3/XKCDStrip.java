package at.ac.tuwien.digital_preservation.task3;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nathaniel on 10.06.16.
 */
public class XKCDStrip {

    public String stripUrl;
    public String fileURL;
    public int fileSize;
    public String stripTitle;
    public Date dateTaken;

    @Override
    public String toString() {
        return "XKCDStrip{" +
                "stripURL='" + stripUrl + '\'' +
                "fileURL='" + fileURL + '\'' +
                ", fileSize=" + fileSize +
                ", stripTitle='" + stripTitle + '\'' +
                ", dateTaken='" + dateTaken + '\'' +
                '}';
    }

    public String toCSVLine() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        return "'" + stripUrl + "','" + fileURL + "'," + fileSize + ",'" + stripTitle + "','" + ((dateTaken != null)?format.format(dateTaken):"") + "'";
    }
}
