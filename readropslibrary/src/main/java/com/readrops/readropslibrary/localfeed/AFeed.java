package com.readrops.readropslibrary.localfeed;

/*
 A simple class to give an abstract level to rss/atom/json feed classes
 */
public abstract class AFeed {

    protected String etag;

    protected String lastModified;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
