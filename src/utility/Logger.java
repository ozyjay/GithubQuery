package utility;

import com.opencsv.CSVWriter;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class Logger implements Closeable {
    private CSVWriter csvWriter;

    private static Logger instance;

    public static Logger getInstance() throws IOException {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private Logger() throws IOException {
        FileWriter fileWriter = new FileWriter("resources/results.csv");
        csvWriter = new CSVWriter(fileWriter);
    }

    public void log(String data) throws IOException {
        csvWriter.writeNext(data.split(","));
        csvWriter.flush();
    }

    @Override
    public void close() throws IOException {
        csvWriter.close();
    }
}
