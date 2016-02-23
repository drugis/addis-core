package org.drugis.addis.trialverse;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by connor on 19-2-16.
 */
public class SparqlQueryTest {

    public static final String GRAPH_PREFIX = "http://trials.drugis.org/graphs/";

    @Test
    public void studyDetailsQuery() {
        String graphUid = "graphuid";
        String queryUnderTest = StringUtils.replace(TriplestoreServiceImpl.STUDY_DETAILS_QUERY, "$studyGraphUid", graphUid);
        Model model = readModelFromFile("simpleTestStudy.ttl");
        Dataset dataset = DatasetFactory.createMem();
        dataset.addNamedModel(GRAPH_PREFIX + graphUid, model);
        Query query = QueryFactory.create(queryUnderTest);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect();
            int numberOfResultRows = 0;
            while (results.hasNext()) {
                ++numberOfResultRows;
                QuerySolution row = results.next();
                assertEquals("http://trials.drugis.org/studies/12345-abcde", row.getResource("studyUri").toString());
                assertEquals("A comparison of once-daily venlafaxine XR and paroxetine in depressed outpatients treated in general practice. Primary Care Psychiatry 1998; 4: 127-132", row.getLiteral("title").toString());
                assertEquals("McPartlin et al, 1998", row.getLiteral("label").toString());
                assertEquals("AllocationRandomized", row.getResource("allocation").getLocalName());
                assertEquals("DoubleBlind", row.getResource("blinding").getLocalName());
                assertEquals("To evaluate the efficacy and safety of venlafaxine XR and paroxetine", row.getLiteral("objective").toString());
                assertEquals("Inclusion criteria:\n\nMale or female outpatients at least 18 years major depression", row.getLiteral("inclusionCriteria").toString());
                assertEquals("StatusCompleted", row.getResource("status").getLocalName() );
                assertEquals(43, row.getLiteral("numberOfCenters").getInt());
                assertEquals("2008-04-01", row.getLiteral("startDate").getValue().toString());
                assertEquals("2009-04-01", row.getLiteral("endDate").getValue().toString());
                assertEquals("Flexible", row.getLiteral("doseType").toString());
                assertEquals("Azilsartan", row.getLiteral("drugNames").toString());
                assertEquals("http://pubmed.com/21282560", row.getLiteral("publications").toString());
                assertEquals(0, row.getLiteral("numberOfArms").getInt());
            }
            assertEquals(1, numberOfResultRows);
        }
    }

    @Test
    public void studyDataQueryEndpoints() {
        String studyUid = "507251cc-86a2-431c-a266-2ced26e35f07";

        String queryUnderTest = TriplestoreServiceImpl.STUDY_DATA;
        queryUnderTest = StringUtils.replace(queryUnderTest, "$studyUid", studyUid);
        queryUnderTest = StringUtils.replace(queryUnderTest, "$studyDataType", StudyDataSection.ENDPOINTS.toString());

        Model studyModel = readModelFromFile("TAK491-019.ttl");
        Model conceptsModel = readModelFromFile("concepts.ttl");
        Dataset dataset = DatasetFactory.createMem();
        dataset.addNamedModel(GRAPH_PREFIX + studyUid, studyModel);
        dataset.addNamedModel(GRAPH_PREFIX + "concepts", conceptsModel);

        int numberOfResultRows = 0;
        Set<String> outcomes = new HashSet<>();
        Set<String> dataTypes = new HashSet<>();
        Set<String> dataTypeLabels = new HashSet<>();
        Set<String> instanceUids = new HashSet<>();
        Set<String> momentUids = new HashSet<>();
        Set<String> groupLabels = new HashSet<>();
        Set<String> relativeToAnchors = new HashSet<>();
        Set<String> timeOffsets = new HashSet<>();
        Set<String> durations = new HashSet<>();
        Set<String> relativeToEpochLabels = new HashSet<>();
        ArrayList<Integer> counts = new ArrayList<>();
        ArrayList<Double> means = new ArrayList<>();
        ArrayList<Double> stds = new ArrayList<>();


        Query query = QueryFactory.create(queryUnderTest);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                ++numberOfResultRows;
                QuerySolution row = results.next();

                System.out.println(row.toString());

                dataTypes.add(row.getResource("studyDataTypeUri").toString());
                dataTypeLabels.add(row.getLiteral("studyDataTypeLabel").toString());
                outcomes.add(row.getResource("outcomeUid").toString());
                instanceUids.add(row.getResource("instanceUid").toString());
                momentUids.add(row.getResource("momentUid").toString());
                if (row.contains("groupLabel") ) {
                    groupLabels.add(row.getLiteral("groupLabel").toString());
                }
                relativeToAnchors.add(row.getResource("relativeToAnchor").toString());
                timeOffsets.add(row.getLiteral("timeOffset").toString());
                durations.add(row.getLiteral("duration").toString());
                relativeToEpochLabels.add(row.getLiteral("relativeToEpochLabel").toString());

                counts.add(row.getLiteral("sampleSize").getInt());
                if (row.contains("mean") ) {
                    means.add(row.getLiteral("mean").getDouble());
                }
                if (row.contains("std") ) {
                    stds.add(row.getLiteral("std").getDouble());
                }

            }

        }

        System.out.println("row count: " + numberOfResultRows);
        assertEquals(78, numberOfResultRows);
        assertEquals(15, outcomes.size());
        assertEquals(15, dataTypes.size());
        assertEquals(15, dataTypeLabels.size());

        assertEquals(7, instanceUids.size());
        String [] expectedArmLabels = {"StudyPopulation", "Placebo QD", "Valsartan 320 mg QD", "Azilsartan Medoxomil 80 mg QD",
                "Azilsartan Medoxomil 40 mg QD", "Olmesartan 40 mg QD", "old people"};
        assertArrayEquals(expectedArmLabels, groupLabels.toArray());
        assertEquals(1, momentUids.size());
        assertEquals(1, relativeToEpochLabels.size());
        assertTrue(relativeToEpochLabels.contains("Main phase"));

        counts.sort(Comparator.naturalOrder());
        assertEquals((Integer) 111, counts.get(0));

        means.sort(Comparator.naturalOrder());
        assertEquals( -16.74D, means.get(0), 0.001D);

        stds.sort(Comparator.naturalOrder());
        assertEquals(1.1D, stds.get(0), 0.0001D);

    }

    private Model readModelFromFile(String fileName) {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(new FileReader("src/test/resources/queryTest/" + fileName), "http://sparqlquerytest.com", RDFLanguages.strLangTurtle);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return model;
    }
}
