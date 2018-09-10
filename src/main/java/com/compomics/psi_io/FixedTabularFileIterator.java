package com.compomics.psi_io;

import com.compomics.psi_io.ionbot.IonbotHeader;
import com.compomics.psi_io.ionbot.IonbotHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Convert a tabular file into an {@link Iterable} that returns {@link Map}<String, String> instances per line that use
 * the values on the first line as keys and the values per line as value. Using this approach one does not have to read
 * the entire file into memory first, providing some much needed relief when parsing large Omega files.
 *
 * @param <T> the header enum
 */
public class FixedTabularFileIterator<T extends Enum<T>> implements Iterable<Map<T, String>>, Iterator<Map<T, String>> {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedTabularFileIterator.class);

    private static final char DELIMITER = '\t';

    private final IonbotHeaders<T> ionbotHeaders;
    private final BufferedReader bufferedReader;
    private String[] nextLine;
    /**
     * The header values map.
     */
    private final EnumMap<T, Integer> headerIndexes;

    /**
     * Initialize an iterator for the data file. When iterating of the rows, the columns with the given headerValues are
     * being parsed and put in a map (key: the {@link IonbotHeader} instance; value: the column entry). The method
     * throws an {@link IllegalArgumentException} if a header is not present in the given file.
     *
     * @param tsvFile      the tab separated data file
     * @param ionbotHeaders the {@link IonbotHeaders} instance
     * @throws IOException              in case of an Input/Output related problem
     * @throws IllegalArgumentException in case on of the given headers is not present
     */
    public FixedTabularFileIterator(final Path tsvFile, IonbotHeaders<T> ionbotHeaders) throws IOException {
        bufferedReader = Files.newBufferedReader(tsvFile);
        this.ionbotHeaders = ionbotHeaders;
        this.headerIndexes = new EnumMap<>(ionbotHeaders.getEnumType());

        //read the first line
        String firstLine = bufferedReader.readLine();

        if (firstLine == null || firstLine.isEmpty()) {
            throw new IOException("Input file " + tsvFile.getFileName() + " is empty.");
        } else {
            firstLine = firstLine.toLowerCase(Locale.US);
        }

        List<String> firstLineList = Arrays.asList(firstLine.split(String.valueOf(DELIMITER)));
        //check if each of the given header values is present in the file header
        ionbotHeaders.getMandatoryHeaders().forEach((ionbotHeader) -> {
            Optional<String> header = ionbotHeader.getValues()
                    .stream()
                    .filter(firstLineList::contains)
                    .findFirst();

            if (header.isPresent()) {
                ionbotHeader.setParsedValue(ionbotHeader.getValues().indexOf(header.get()));
                headerIndexes.put(Enum.valueOf(ionbotHeaders.getEnumType(), ionbotHeader.getName()), firstLineList.indexOf(header.get()));
            } else {
                throw new IllegalArgumentException("The mandatory header " + ionbotHeader.getName() + " is not present in the given file " + tsvFile.getFileName());
            }
        });

        advanceLine();
    }

    @Override
    public boolean hasNext() {
        if (nextLine == null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return nextLine != null;
    }

    @Override
    public EnumMap<T, String> next() {
        EnumMap<T, String> lineValues = new EnumMap<>(ionbotHeaders.getEnumType());

        headerIndexes.entrySet().stream().forEach((entry) -> lineValues.put(entry.getKey(), nextLine[entry.getValue()]));

        //advance to the next line for the next invocation
        advanceLine();

        return lineValues;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This parser does not support removing lines from a file.");
    }

    @Override
    public Iterator<Map<T, String>> iterator() {
        // We want to be an iterable and an iterator at the same time
        return this;
    }

    /**
     * Parse the next line and split its values by the configured delimiter. Also handles the end of file by setting
     * nextLine to null.
     */
    private void advanceLine() {
        try {
            String readLine = bufferedReader.readLine();
            if (readLine != null) {
                nextLine = readLine.split(String.valueOf(DELIMITER), -1);
                return;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        nextLine = null;
    }
}
