package utility;

import com.opencsv.CSVWriter;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Logger implements Closeable {
    private static Map<String, CSVWriter> logs;
    private static CSVWriter currentWriter;
    private static Logger instance;

    private Logger() {
        logs = new HashMap<>();
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public Logger with(String name) {
        currentWriter = logs.get(name);
        return instance;
    }

    public void log(String data) throws IOException {
        if (currentWriter == null) return;

        currentWriter.writeNext(data.split(","));
        currentWriter.flush();
    }

    @Override
    public void close() throws IOException {
        for (CSVWriter writer : logs.values())
            writer.close();
    }

    public void create(String logger, String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        logs.put(logger, csvWriter);
    }
}
