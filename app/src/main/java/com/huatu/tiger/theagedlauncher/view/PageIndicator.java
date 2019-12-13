package com.huatu.tiger.theagedlauncher.view;

/**
 * Base class for a page indicator.
 */
public interface PageIndicator {

    void setScroll(int currentScroll, int totalScroll);

    void setActiveMarker(int activePage);

    void setMarkersCount(int numMarkers);
}