package jp.kyoto.nlp.kanken;

import java.util.Date;

public class ResultsHistoryItem {

    private Date date;
    private int readingRights;
    private int readingWrongs;
    private int writingRights;
    private int writingWrongs;

    public ResultsHistoryItem(Date date, int readingRights, int readingWrongs, int writingRights, int writingWrongs) {
        this.date = date;
        this.readingRights = readingRights;
        this.readingWrongs = readingWrongs;
        this.writingRights = writingRights;
        this.writingWrongs = writingWrongs;
    }

    public Date getDate() {
        return date;
    }

    public int getReadingRights() {
        return readingRights;
    }

    public int getReadingWrongs() {
        return readingWrongs;
    }

    public int getWritingRights() {
        return writingRights;
    }

    public int getWritingWrongs() {
        return writingWrongs;
    }

    public int getTotalRights() {
        return readingRights + writingRights;
    }

    public int getTotalWrongs() {
        return writingRights + writingWrongs;
    }

    @Override
    public String toString() {
        return "ResultsHistoryItem{" +
                "date='" + date + '\'' +
                ", readingRights='" + readingRights + '\'' +
                ", readingWrongs=" + readingWrongs +
                ", writingRights='" + writingRights + '\'' +
                ", writingWrongs='" + writingWrongs + '}';
    }
}
