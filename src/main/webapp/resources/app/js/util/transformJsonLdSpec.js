'use strict';
define(['util/transformJsonLd'], function(transformJsonLd) {
  fdescribe('transformJsonLd', function() {

    var emptyStudy = {
      "@id": "http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6",
      "@type": "http://trials.drugis.org/ontology#Study",
      "http://trials.drugis.org/ontology#has_epochs": {
        "@list": []
      },
      "label": "study 1",
      "comment": "my study",
      "@context": {
        "label": "http://www.w3.org/2000/01/rdf-schema#label",
        "comment": "http://www.w3.org/2000/01/rdf-schema#comment",
        "has_epochs": {
          "@id": "http://trials.drugis.org/ontology#has_epochs",
          "@type": "@id"
        }
      }
    };

    var emptyTransformed = {
      "@graph": [{
        "@id": "http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6",
        "@type": "ontology:Study",
        "has_epochs": [],
        "label": "study 1",
        "comment": "my study",
        "has_outcome": [],
        "has_arm": [],
        "has_activity": [],
        "has_indication": [],
        "has_objective": [],
        "has_publication": [],
        "has_eligibility_criteria": []
      }],
      "@context": {
        "label": "http://www.w3.org/2000/01/rdf-schema#label",
        "comment": "http://www.w3.org/2000/01/rdf-schema#comment",
        "has_epochs": {
          "@id": "http://trials.drugis.org/ontology#has_epochs",
          "@container": "@list"
        },
        "ontology": "http://trials.drugis.org/ontology#"
      }
    };


    it('should transform an empty study', function() {
      expect(transformJsonLd(emptyStudy)).toEqual(emptyTransformed);
    });



  });
});