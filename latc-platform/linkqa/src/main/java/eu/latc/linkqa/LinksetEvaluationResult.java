package eu.latc.linkqa;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/25/11
 *         Time: 12:22 AM
 */
public class LinksetEvaluationResult
{
    private DatasetDesc refset;
    private DatasetDesc linkset;
    private PrecResult precEvalResult;

    private int duplicateCacheLimit;
    private int duplicateCacheUsage;
    //private int timeTaken; // in ms
    private Calendar startDate;
    private Calendar endDate;


    LinksetEvaluationResult(PrecResult precEvalResult, DatasetDesc linkset, DatasetDesc refset, Calendar startDate, Calendar endDate, int duplicateCacheLimit, int duplicateCacheUsage) {
        this.precEvalResult = precEvalResult;
        this.linkset = linkset;
        this.refset = refset;
        this.duplicateCacheLimit = duplicateCacheLimit;
        this.duplicateCacheUsage = duplicateCacheUsage;
        //this.timeTaken = timeTaken;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DatasetDesc getRefset() {
        return refset;
    }

    public void setRefset(DatasetDesc refset) {
        this.refset = refset;
    }

    public DatasetDesc getLinkset() {
        return linkset;
    }

    public void setLinkset(DatasetDesc linkset) {
        this.linkset = linkset;
    }

    public PrecResult getPrecEvalResult() {
        return precEvalResult;
    }

    public void setPrecEvalResult(PrecResult precEvalResult) {
        this.precEvalResult = precEvalResult;
    }

    public long getTimeTaken() {
        //return timeTaken;
        return endDate.getTimeInMillis() - startDate.getTimeInMillis();
    }

    /*
    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }*/

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public int getDuplicateCacheLimit() {
        return duplicateCacheLimit;
    }

    public void setDuplicateCacheLimit(int duplicateCacheLimit) {
        this.duplicateCacheLimit = duplicateCacheLimit;
    }

    public int getDuplicateCacheUsage() {
        return duplicateCacheUsage;
    }

    public void setDuplicateCacheUsage(int duplicateCacheUsage) {
        this.duplicateCacheUsage = duplicateCacheUsage;
    }
}
