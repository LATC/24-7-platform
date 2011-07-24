package eu.latc.linkqa;

import org.apache.hadoop.fs.Path;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/25/11
 *         Time: 12:21 AM
 */
public class DatasetDesc
{
    private Path location;

    //private Date
    private int totalTripleCount;
    private int duplicateCount;
    private int effectiveTripleCount;


    public DatasetDesc()
    {
    }

    public DatasetDesc(Path location)
    {
        this.location = location;
    }

    public DatasetDesc(Path location, int totalTripleCount, int duplicateCount, int effectiveTripleCount) {
        this.location = location;
        this.totalTripleCount = totalTripleCount;
        this.duplicateCount = duplicateCount;
        this.effectiveTripleCount = effectiveTripleCount;
    }

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public int getTotalTripleCount() {
        return totalTripleCount;
    }

    public void setTotalTripleCount(int totalTripleCount) {
        this.totalTripleCount = totalTripleCount;
    }

    public int getDuplicateTripleCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getEffectiveTripleCount() {
        return effectiveTripleCount;
    }

    public void setEffectiveTripleCount(int effectiveTripleCount) {
        this.effectiveTripleCount = effectiveTripleCount;
    }
}
