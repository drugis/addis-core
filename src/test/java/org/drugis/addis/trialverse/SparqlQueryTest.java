package org.drugis.addis.trialverse;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;

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
