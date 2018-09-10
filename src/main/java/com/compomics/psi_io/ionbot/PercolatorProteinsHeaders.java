package com.compomics.psi_io.ionbot;

import java.io.IOException;
import java.util.EnumMap;

/**
 * Created by Niels on 03/03/2015.
 */
public class PercolatorProteinsHeaders extends IonbotHeaders<PercolatorProteinsHeader> {

    /**
     * No-arg constructor.
     */
    public PercolatorProteinsHeaders() throws IOException {
        super(PercolatorProteinsHeader.class, new EnumMap<>(PercolatorProteinsHeader.class), "ionbot/ionbot_percolator_proteins.json");
    }
}
