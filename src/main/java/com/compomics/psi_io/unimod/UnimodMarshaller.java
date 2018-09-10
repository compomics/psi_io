package com.compomics.psi_io.unimod;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to marshall and access the UNIMOD modifications from the unimod.xml file.
 * <p/>
 * Created by niels on 5/02/15.
 */
public class UnimodMarshaller {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UnimodMarshaller.class);

    private static final Namespace NAMESPACE = Namespace.getNamespace("http://www.unimod.org/xmlns/schema/unimod_2");
    private static final String UNIMOD_ACCESSION = "UNIMOD:%d";

    /**
     * This maps holds the modifications from the parsed UNIMOD .xml file. This map can contains instances of {@link
     * UnimodModification}. The key is the UNIMOD record ID.
     */
    private final Map<Integer, UnimodModification> modifications = new HashMap<>();

    public Map<Integer, UnimodModification> getModifications() {
        return modifications;
    }

    public UnimodMarshaller() throws JDOMException, IOException {
        marshal();
    }

    /**
     * Get the modification by accession. Returns null if nothing was found.
     *
     * @param accession the modification accession
     * @return the found search modification, null if nothing was found
     */
    public UnimodModification getModificationByAccession(Integer accession) {
        UnimodModification modification = null;

        if (modifications.containsKey(accession)) {
            modification = modifications.get(accession);
        }

        return modification;
    }

    /**
     * Get the modification by name. Returns null if nothing was found.
     *
     * @param name the modification name
     * @return the found search modification, null if nothing was found
     */
    public UnimodModification getModificationByName(String name) {
        UnimodModification modification = null;

        Optional<UnimodModification> foundModification = modifications
                .values()
                .stream()
                .filter(filteredModification -> filteredModification.getName().equals(name))
                .findFirst();

        if (foundModification.isPresent()) {
            modification = foundModification.get();
        }

        return modification;
    }

    /**
     * This method marshals the UNIMOD file and puts all modifications in the map for later usage.
     *
     * @throws JDOMException top level exception that can be thrown in case of a problem in the JDOM classes.
     * @throws IOException   in case of a problem with the unimod.xml recource
     */
    private void marshal() throws JDOMException, IOException {
        Resource unimodResource = new ClassPathResource("unimod/unimod.xml");
        SAXBuilder builder = new SAXBuilder();

        Document document;
        document = builder.build(unimodResource.getInputStream());

        Element root = document.getRootElement();
        //get the modifications element
        Element modificationsElement = root.getChild("modifications", NAMESPACE);

        for (Element modificationElement : modificationsElement.getChildren("mod", NAMESPACE)) {
            //get the mod name and record ID
            Attribute title = modificationElement.getAttribute("title");
            Attribute record = modificationElement.getAttribute("record_id");

            //create the modification instance
            UnimodModification modification = new UnimodModification(record.getIntValue(), title.getValue());

            //get the affected amino acids, only consider the ones that are not hidden
            Map<String, String> affectedAminoAcids = new HashMap<>();
            modificationElement.getChildren("specificity", NAMESPACE).stream()
                    .filter(specificity -> specificity.getAttribute("hidden").getValue().equals("0"))
                    .forEach(element -> {
                        affectedAminoAcids.put(element.getAttribute("site").getValue(), element.getAttribute("position").getValue());
                    });
            if (!affectedAminoAcids.isEmpty()) {
                modification.setAffectedAminoAcids(affectedAminoAcids);
            }

            //get the mono isotopic and average mass shift
            Element delta = modificationElement.getChild("delta", NAMESPACE);
            Attribute monoIsotopicMassShift = delta.getAttribute("mono_mass");
            Attribute averageMassShift = delta.getAttribute("avge_mass");

            modification.setMonoIsotopicMassShift(monoIsotopicMassShift.getDoubleValue());
            modification.setAverageMassShift(averageMassShift.getDoubleValue());

            modifications.put(record.getIntValue(), modification);
        }
    }

}
