package org.nl.bot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.impl.CandleImpl;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryUtils {

    public static final String DELIMETER = "-------------------";

    public static void save(@Nonnull String path, @Nonnull List<Candle> candleList) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        File file = new File(path);

        FileWriter out = null;
        try {
            out = new FileWriter(file);
            for (Candle candle : candleList) {
                out.write(objectMapper.writeValueAsString(candle));
                out.write("\n");
                out.write(DELIMETER);
                out.write("\n");
                out.flush();
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    public static List<Candle> read(@Nonnull String path) {
        List<Candle> res = new ArrayList<>();

        StringBuffer buff = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }

                if (DELIMETER.equals(s)) {
                    res.add(objectMapper.readValue(buff.toString(), CandleImpl.class));
                    buff = new StringBuffer();
                } else {
                    buff.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
}
