package com.compomics.psi_io.unimod;

import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author Niels Hulstaert
 */
public class UnimodMarshallerTest {

    private static final String MOD_NAME = "Dioxidation";
    private static final Integer MOD_ACCESSION = 425;

    private UnimodMarshaller unimodMarshaller;

    @Before
    public void setUp() throws Exception {
        unimodMarshaller = new UnimodMarshaller();
    }

    /**
     * Test the marshalling of the unimod.xml file.
     *
     * @throws JDOMException top level exception that can be thrown in case of a
     * problem in the JDOM classes.
     */
    @Test
    public void testMarshall() throws JDOMException {
        Assert.assertFalse(unimodMarshaller.getModifications().isEmpty());
    }

    /**
     * Test the retrieval of a modification from the marshaller of both
     * Modification and SearchModification instances.
     */
    @Test
    public void testGetModificationByAccession() {
        UnimodModification modification = unimodMarshaller.getModificationByAccession(MOD_ACCESSION);
        Assert.assertNotNull(modification);

        modification = unimodMarshaller.getModificationByAccession(MOD_ACCESSION);
        Assert.assertNotNull(modification);
        Assert.assertEquals(MOD_ACCESSION, modification.getAccession());
        Assert.assertEquals(MOD_NAME, modification.getName());
        Assert.assertEquals(31.989829, modification.getMonoIsotopicMassShift(), 0.0001);
        Assert.assertEquals(31.9988, modification.getAverageMassShift(), 0.0001);
        Map<String, String> affectedAminoAcids = modification.getAffectedAminoAcids();
        Assert.assertFalse(affectedAminoAcids.isEmpty());
        Assert.assertTrue(affectedAminoAcids.containsKey("M"));
        Assert.assertEquals(affectedAminoAcids.get("M"), "Anywhere");
    }

    /**
     * Test the retrieval of a modification from the marshaller of both
     * Modification and SearchModification instances.
     */
    @Test
    public void testGetModificationByName() {
        UnimodModification modification = unimodMarshaller.getModificationByName(MOD_NAME);
        Assert.assertNotNull(modification);

        modification = unimodMarshaller.getModificationByName(MOD_NAME);
        Assert.assertNotNull(modification);
        Assert.assertEquals(MOD_ACCESSION, modification.getAccession());
        Assert.assertEquals(MOD_NAME, modification.getName());
        Assert.assertEquals(31.989829, modification.getMonoIsotopicMassShift(), 0.0001);
        Assert.assertEquals(31.9988, modification.getAverageMassShift(), 0.0001);
    }

}
