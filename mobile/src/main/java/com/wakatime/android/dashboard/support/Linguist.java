package com.wakatime.android.dashboard.support;

import android.content.Context;
import android.graphics.Color;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wakatime.android.support.chart.RandomColor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Converts languages into colors, based on Github's linguist data
 *
 * @author Joao Pedro Evangelista
 */
public class Linguist {

    private final Map<String, Map<String, Object>> languages;

    private Linguist(Map<String, Map<String, Object>> languages) {
        this.languages = languages;
    }

    public static Linguist init(Context context) {
        try {
            InputStream data = context.getAssets().open("linguist.json");

            Map<String, Map<String, Object>> value = new ObjectMapper()
                    .readValue(data,
                            new TypeReference<Map<String, Map<String, Object>>>() {
                            });

            return new Linguist(value);
        } catch (IOException e) {
            Timber.e(e, "Could not fetch linguist file from assets folder");
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Find the matching language on the map and return a int code of color
     *
     * @param language the language to look for
     * @return int code of color
     */
    public int decode(String language) {
        Map<String, Object> languageMap = languages.get(language);
        if (languageMap == null) {
            return RandomColor.generate();
        } else {
            Object color = languageMap.get("color");
            if (color != null) {
                return Color.parseColor(color.toString());
            } else {
                return RandomColor.generate();
            }
        }
    }

    public int decodeOS(String os) {
        String color;
        switch (os.toLowerCase(Locale.US)) {
            case "windows": color ="#00bcf2";
                break;
            case "mac": color = "#8e8e93";
                break;
            case "linux": color = "#dd4814";
                break;
            default: color = "#e74c3c";
        }

        return Color.parseColor(color);
    }
}
