package com.compomics.psi_io.ionbot;

import java.io.IOException;
import java.util.EnumMap;

/**
 * Created by Niels on 03/03/2015.
 */
public class MainHeaders extends IonbotHeaders<MainHeader> {

    /**
     * No-arg constructor.
     */
    public MainHeaders() throws IOException {
        super(MainHeader.class, new EnumMap<>(MainHeader.class), "ionbot/ionbot_main.json");
    }
}
