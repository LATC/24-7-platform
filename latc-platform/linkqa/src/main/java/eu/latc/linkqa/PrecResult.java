package eu.latc.linkqa;

import org.aksw.commons.collections.SampleStats;

/**
 * Precision recall result
 *
 * @author Claus Stadler
 *
 *         Date: 7/25/11
 *         Time: 12:21 AM
 */
public class PrecResult
{
    private long linksetSize;
    private long refsetSize;
    private long overlapSize;

    public PrecResult(long linksetSize, long refsetSize, long overlap)
    {
        this.linksetSize = linksetSize;
        this.refsetSize = refsetSize;
        this.overlapSize = overlap;
    }

    public static PrecResult create(long linksetSize, long refsetSize, long overlap)
    {
        return new PrecResult(linksetSize, refsetSize, overlap);
    }

    /**
     * What ratio of entities of the refset have been found?
     *
     * @return
     */
    public double getRecall() {
        return overlapSize / (double)refsetSize;
    }

    /**
     * What ratio of entities in the linkset occurr in the refset?
     *
     * @return
     */
    public double getPrecision() {
        return overlapSize / (double)linksetSize;
    }

    public double getFMeasure() {
        return SampleStats.fMeasure(getPrecision(), getRecall());
    }

    public long getLinksetSize() {
        return linksetSize;
    }


    public long getRefsetSize() {
        return refsetSize;
    }

    public long getOverlapSize() {
        return overlapSize;
    }

    @Override
    public String toString() {
        return "PrecResult{" +
                "linksetSize=" + linksetSize +
                ", refsetSize=" + refsetSize +
                ", overlapSize=" + overlapSize +
                '}';
    }
}
