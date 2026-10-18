package com.example.filmservice.service;

import com.example.filmservice.entity.Film;

import java.io.IOException;
import java.io.Writer;

/** Writes films as CSV rows (RFC 4180 quoting). */
class FilmCsvWriter {

    /** Lets Excel detect UTF-8 when the file is opened directly. */
    private static final char UTF8_BOM = '\uFEFF';
    private static final String HEADER = "id,title,year,country,genres,director";
    private static final String LINE_END = "\r\n";

    private final Writer writer;

    FilmCsvWriter(Writer writer) {
        this.writer = writer;
    }

    void writeHeader() throws IOException {
        writer.write(UTF8_BOM);
        writer.write(HEADER);
        writer.write(LINE_END);
    }

    void writeRow(Film film) throws IOException {
        writer.write(String.join(",",
                String.valueOf(film.getId()),
                escape(film.getTitle()),
                String.valueOf(film.getYear()),
                escape(film.getCountry()),
                escape(film.getGenres()),
                escape(film.getDirector().getName())));
        writer.write(LINE_END);
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
