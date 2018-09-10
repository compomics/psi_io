package com.compomics.psi_io.mzidentml;

import java.nio.file.Path;
import java.util.List;

/**
 * This class holds the necessary resources for an mzIdentML export.
 *
 * @author Niels Hulstaert
 */
public class MzIdentMlExport {

    /**
     * The FASTA file directory.
     */
    private Path fastasDirectory;
    /**
     * The mzIdentML export file path.
     */
    private Path mzIdentMlPath;
    /**
     * The MGF export file path;
     */
    private Path mgfPath;

    /**
     * Constructor.
     *
     * @param fastasDirectory the FASTA directory path
     * @param mzIdentMlPath the mzIdentML export file path
     * @param mgfPath the MGF export file path
     * @param analyticalRuns the list of analytical runs
     * @param user the User instance
     */
    /*public MzIdentMlExport(Path fastasDirectory, Path mzIdentMlPath, Path mgfPath, List<AnalyticalRun> analyticalRuns, User user) {
        this.fastasDirectory = fastasDirectory;
        this.mzIdentMlPath = mzIdentMlPath;
        this.mgfPath = mgfPath;
        this.analyticalRuns = analyticalRuns;
        this.user = user;
    }

    public Path getFastasDirectory() {
        return fastasDirectory;
    }

    public Path getMzIdentMlPath() {
        return mzIdentMlPath;
    }

    public Path getMgfPath() {
        return mgfPath;
    }

    public List<AnalyticalRun> getAnalyticalRuns() {
        return analyticalRuns;
    }

    public User getUser() {
        return user;
    }*/

}
