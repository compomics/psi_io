package com.compomics.psi_io.unimod;

import java.util.HashMap;
import java.util.Map;

public class UnimodModification {

    private Integer accession;

    private String name;

    private Double monoIsotopicMassShift;

    private Double averageMassShift;

    private Map<String, String> affectedAminoAcids = new HashMap<>();

    public UnimodModification(Integer accession, String name) {
        this.accession = accession;
        this.name = name;
    }

    public Integer getAccession() {
        return accession;
    }

    public void setAccession(Integer accession) {
        this.accession = accession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMonoIsotopicMassShift() {
        return monoIsotopicMassShift;
    }

    public void setMonoIsotopicMassShift(Double monoIsotopicMassShift) {
        this.monoIsotopicMassShift = monoIsotopicMassShift;
    }

    public Double getAverageMassShift() {
        return averageMassShift;
    }

    public void setAverageMassShift(Double averageMassShift) {
        this.averageMassShift = averageMassShift;
    }

    public Map<String, String> getAffectedAminoAcids() {
        return affectedAminoAcids;
    }

    public void setAffectedAminoAcids(Map<String, String> affectedAminoAcids) {
        this.affectedAminoAcids = affectedAminoAcids;
    }
}
