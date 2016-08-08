package com.hyunnyapp.brainylivingroom.weathergatter;

public interface YahooWeatherExceptionListener {
    void onFailConnection(final Exception e);
    void onFailParsing(final Exception e);
    void onFailFindLocation(final Exception e);
}
