package com.compomics.psi_io.fasta;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class represents a FASTA database in the database.
 *
 * @author Niels Hulstaert
 */
public class FastaDb  {

    /**
     * The official name of the FASTA db.
     */
    private String name;
    /**
     * The name of the FASTA db file.
     */
    private String fileName;
    /**
     * The FASTA db file path.
     */
    private String filePath;
    /**
     * The database name of the FASTA db.
     */
    private String databaseName;
    /**
     * The version of the FASTA db.
     */
    private String version;
    /**
     * The MD5 checksum of the FASTA db.
     */
    private String md5CheckSum;
    /**
     * The regular expression for parsing the FASTA headers to extract the
     * protein accession.
     */
    private String headerParseRule;
    /**
     * The species taxonomy ID.
     */
    private Integer taxonomyID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMd5CheckSum() {
        return md5CheckSum;
    }

    public void setMd5CheckSum(String md5CheckSum) {
        this.md5CheckSum = md5CheckSum;
    }

    public String getHeaderParseRule() {
        return headerParseRule;
    }

    public void setHeaderParseRule(String headerParseRule) {
        this.headerParseRule = headerParseRule;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

}
