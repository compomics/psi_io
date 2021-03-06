package com.compomics.psi_io.fasta;

import com.compomics.util.protein.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses FASTA files (protein accession and sequence).
 * <p>
 * Created by Niels Hulstaert on 7/10/16.
 */
public class FastaDbParser {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FastaDbParser.class);

    private static final String BLOCK_SEPARATOR = ">";
    private static final String SPLITTER = " ";
    private static final String PARSE_RULE_SPLITTER = ";";

    /**
     * Parse the given FASTA files into a map of protein accession -> sequence pairs. This method takes a {@link
     * LinkedHashMap} of {@link FastaDb} instances as keys as an argument to consistently handle possible duplicate
     * accessions between different FASTA DB files. In case of a duplicate accession, the associated protein sequence of
     * the first entry is put in the map.
     *
     * @param fastaDbs the FASTA files to parse and their associated (absolute) path
     * @return the protein sequences map (key: protein accession; value: protein sequence)
     * @throws IOException thrown in case of an input/output related problem
     */
    public Map<String, String> parse(LinkedHashMap<FastaDb, Path> fastaDbs) throws IOException {
        Map<String, String> proteinSequences = new HashMap<>();
        try {
            for (Map.Entry<FastaDb, Path> entry : fastaDbs.entrySet()) {
                FastaDb fastaDb = entry.getKey();
                Path fastaPath = entry.getValue();
                //check if the FASTA has an associated header parse rule and parse accordingly
                //otherwise, use the Compomics Utilities library
                if (fastaDb.getHeaderParseRule() == null || fastaDb.getHeaderParseRule().equals("") || fastaDb.getHeaderParseRule().equals("none")) {
                    parseWithoutRule(proteinSequences, fastaPath);
                } else {
                    parseWithRule(proteinSequences, fastaDb, fastaPath);
                }
                if (proteinSequences.isEmpty()) {
                    throw new IllegalStateException("No accessions could be parsed from the FASTA DB file(s). Are you using the correct parse rule?");
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Error parsing FASTA file, please check that it contains valid data");
        }

        return proteinSequences;
    }

    /**
     * Parse the protein accessions from the given FASTA files into a map (key: the {@link FastaDb} instance; value: the
     * set of protein accessions). The argument is a {@link LinkedHashMap} to be able to return the the parsed
     * accessions in the same order as they were passed.
     *
     * @param fastaDbs the FASTA files to parse and their associated (absolute) path
     * @return the map of parsed accessions (key: the {@link FastaDb} instance; value: the set of protein accessions).
     * @throws IOException           thrown in case of an input/output related problem
     * @throws IllegalStateException if the set of parsed accessions of the one of the FASTA DB files is empty
     */
    public LinkedHashMap<FastaDb, Set<String>> parseAccessions(LinkedHashMap<FastaDb, Path> fastaDbs) throws IOException {
        LinkedHashMap<FastaDb, Set<String>> parsedFastas = new LinkedHashMap<>();
        try {
            for (Map.Entry<FastaDb, Path> entry : fastaDbs.entrySet()) {
                FastaDb fastaDb = entry.getKey();
                Path fastaPath = entry.getValue();
                Set<String> accessions;
                //check if the FASTA has an associated header parse rule and parse accordingly
                if (fastaDb.getHeaderParseRule() == null || fastaDb.getHeaderParseRule().equals("") || fastaDb.getHeaderParseRule().equals("none")) {
                    accessions = parseAccessionsWithoutRule(fastaPath);
                } else {
                    accessions = parseAccessionsWithRule(fastaDb, fastaPath);
                }
                //@TODO check if the utilties parser can be used when no parse rules are given
                //accessions = parseAccessionsWithUtilities(fastaPath);
                if (accessions.isEmpty()) {
                    throw new IllegalStateException("No accessions could be parsed from FASTA DB file " + entry.getValue().toString() + ". Are you using the correct parse rule?");
                }
                parsedFastas.put(fastaDb, accessions);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Error parsing FASTA file, please check that it contains valid data");
        }

        return parsedFastas;
    }

    /**
     * Test the header parse rule for the given FASTA DB file.
     *
     * @param fastaPath       the FASTA DB file path
     * @param parseRule       the header parse rule
     * @param numberOfHeaders the number of headers that will be returned
     * @return the map of parsed headers (key: the parsed accession; value: the original header)
     * @throws IOException           thrown in case of an input/output related problem
     * @throws IllegalStateException if the set of parsed accessions of the one of the FASTA DB files is empty
     */
    public LinkedHashMap<String, String> testParseRule(Path fastaPath, String parseRule, int numberOfHeaders) throws IOException {
        LinkedHashMap<String, String> headers;
        try {
            //check if the FASTA has an associated header parse rule and parse accordingly
            //otherwise, use the Compomics Utilities library
            if (parseRule == null || parseRule.equals("") || parseRule.equals("none")) {
                headers = testParseWithoutRule(fastaPath, numberOfHeaders);
            } else {
                headers = testParseWithRule(fastaPath, parseRule, numberOfHeaders);
            }
            if (headers.isEmpty()) {
                throw new IllegalStateException("No accessions could be parsed from FASTA DB file " + fastaPath + ".");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Error parsing FASTA file, please check that it contains valid data");
        }

        return headers;
    }

    /**
     * Parse the given FASTA file in case a header parse rule is present.
     *
     * @param proteinSequences the protein sequences map
     * @param fastaDb          the {@link FastaDb} instance
     * @param fastaPath        the FASTA path
     * @throws IOException in case of file reading related problem
     */
    private void parseWithRule(Map<String, String> proteinSequences, FastaDb fastaDb, Path fastaPath) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //compile the pattern
            Pattern pattern;
            if (fastaDb.getHeaderParseRule().contains(PARSE_RULE_SPLITTER)) {
                pattern = Pattern.compile(fastaDb.getHeaderParseRule().split(PARSE_RULE_SPLITTER)[1]);
            } else {
                pattern = Pattern.compile(fastaDb.getHeaderParseRule());
            }
            //start reading the file
            final StringBuilder sequenceBuilder = new StringBuilder();
            String fastaHeader = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    //add limiting check for protein store to avoid growing
                    if (sequenceBuilder.length() > 0) {
                        Matcher matcher = pattern.matcher(fastaHeader.substring(1));
                        if (matcher.find()) {
                            proteinSequences.putIfAbsent(matcher.group(1), sequenceBuilder.toString().trim());
                        } else {
                            proteinSequences.putIfAbsent(fastaHeader.substring(1).split(SPLITTER)[0], sequenceBuilder.toString().trim());
                        }
                        sequenceBuilder.setLength(0);
                    }
                    fastaHeader = line;
                } else {
                    sequenceBuilder.append(line);
                }
            }
            //last line
            if (sequenceBuilder.length() > 0) {
                Matcher matcher = pattern.matcher(fastaHeader.substring(1).split(SPLITTER)[0]);
                if (matcher.find()) {
                    proteinSequences.putIfAbsent(matcher.group(1), sequenceBuilder.toString().trim());
                } else {
                    proteinSequences.putIfAbsent(fastaHeader.substring(1).split(SPLITTER)[0], sequenceBuilder.toString().trim());
                }
                sequenceBuilder.setLength(0);
            }
        }
    }

    /**
     * Parse the given FASTA file in case no header parse rule is present.
     *
     * @param proteinSequences the protein sequences map
     * @param fastaPath        the FASTA path
     * @throws IOException in case of file reading related problem
     */
    private void parseWithoutRule(Map<String, String> proteinSequences, Path fastaPath) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //start reading the file
            final StringBuilder sequenceBuilder = new StringBuilder();
            String fastaHeader = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    //add limiting check for protein store to avoid growing
                    if (sequenceBuilder.length() > 0) {
                        proteinSequences.putIfAbsent(fastaHeader.substring(1).split(SPLITTER)[0], sequenceBuilder.toString().trim());
                        sequenceBuilder.setLength(0);
                    }
                    fastaHeader = line;
                } else {
                    sequenceBuilder.append(line);
                }
            }
            //last line
            if (sequenceBuilder.length() > 0) {
                proteinSequences.putIfAbsent(fastaHeader.substring(1).split(SPLITTER)[0], sequenceBuilder.toString().trim());
                sequenceBuilder.setLength(0);
            }
        }
    }

    /**
     * Parse the given FASTA file in case a header parse rule is present.
     *
     * @param fastaDb   the {@link FastaDb} instance
     * @param fastaPath the FASTA path
     * @return the set of parsed protein accessions
     * @throws IOException in case of file reading related problem
     */
    private Set<String> parseAccessionsWithRule(FastaDb fastaDb, Path fastaPath) throws IOException {
        Set<String> accessions = new HashSet<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //compile the pattern
            Pattern pattern;
            if (fastaDb.getHeaderParseRule().contains(PARSE_RULE_SPLITTER)) {
                pattern = Pattern.compile(fastaDb.getHeaderParseRule().split(PARSE_RULE_SPLITTER)[1]);
            } else {
                pattern = Pattern.compile(fastaDb.getHeaderParseRule());
            }
            //start reading the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    Matcher matcher = pattern.matcher(line.substring(1));
                    if (matcher.find()) {
                        accessions.add(matcher.group(1));
                    } else {
                        accessions.add(line.substring(1).split(SPLITTER)[0]);
                    }
                }
            }
        }

        return accessions;
    }

    /**
     * Parse the given FASTA file in case no header parse rule is present.
     *
     * @param fastaPath the FASTA path
     * @return the set of parsed protein accessions
     * @throws IOException in case of file reading related problem
     */
    private Set<String> parseAccessionsWithoutRule(Path fastaPath) throws IOException {
        Set<String> accessions = new HashSet<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //start reading the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    accessions.add(line.substring(1).split(SPLITTER)[0]);
                }
            }
        }

        return accessions;
    }

    /**
     * Parse the given FASTA file with the Compomics utilities library.
     *
     * @param fastaPath the FASTA path
     * @return the set of parsed protein accessions
     * @throws IOException in case of file reading related problem
     */
    private Set<String> parseAccessionsWithUtilities(Path fastaPath) throws IOException {
        Set<String> accessions = new HashSet<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //start reading the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    Header header = Header.parseFromFASTA(line);
                    accessions.add(header.getAccessionOrRest());
                }
            }
        }

        return accessions;
    }

    /**
     * Parse the given FASTA file in case a header parse rule is present.
     *
     * @param fastaPath       the FASTA DB path
     * @param parseRule       the header parse rule
     * @param numberOfHeaders the number of headers that will be parsed
     * @return the set of parsed protein accessions
     * @throws IOException in case of file reading related problem
     */
    private LinkedHashMap<String, String> testParseWithRule(Path fastaPath, String parseRule, int numberOfHeaders) throws IOException {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //compile the pattern
            Pattern pattern;
            if (parseRule.contains(PARSE_RULE_SPLITTER)) {
                pattern = Pattern.compile(parseRule.split(PARSE_RULE_SPLITTER)[1]);
            } else {
                pattern = Pattern.compile(parseRule);
            }
            //start reading the file
            String line;
            while ((line = bufferedReader.readLine()) != null && headers.size() < numberOfHeaders) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    Matcher matcher = pattern.matcher(line.substring(1));
                    if (matcher.find()) {
                        headers.put(matcher.group(1), line);
                    } else {
                        headers.put(line.substring(1).split(SPLITTER)[0], line);
                    }
                }
            }
        }

        return headers;
    }

    /**
     * Parse the given FASTA file in case no header parse rule is present.
     *
     * @param fastaPath       the FASTA DB path
     * @param numberOfHeaders the number of headers that will be parsed
     * @return the set of parsed protein accessions
     * @throws IOException in case of file reading related problem
     */
    private LinkedHashMap<String, String> testParseWithoutRule(Path fastaPath, int numberOfHeaders) throws IOException {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(fastaPath)) {
            //start reading the file
            String line;
            while ((line = bufferedReader.readLine()) != null && headers.size() < numberOfHeaders) {
                if (line.startsWith(BLOCK_SEPARATOR)) {
                    //@TODO return the unparsed header or let compomics utilities try to parse it?
                    Header header = Header.parseFromFASTA(line);
                    headers.put(header.getAccessionOrRest(), line);
                }
            }
        }

        return headers;
    }

}
