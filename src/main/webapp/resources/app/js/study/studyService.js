'use strict';
define([], function() {
  var dependencies = ['$q', '$filter', 'UUIDService', 'RemoteRdfStoreService', 'SparqlResource', 'SanitizeService', 'GraphResource'];
  var StudyService = function($q, $filter, UUIDService, RemoteRdfStoreService, SparqlResource, SanitizeService, GraphResource) {

    var graphPrefix = 'http://trials.drugis.org/graphs/';
    var loadDefer = $q.defer();
    var scratchStudyUri,
      modified = false;
    var studyJsonPromise;
    var context = {
      "standard_deviation": {
        "@id": "http://trials.drugis.org/ontology#standard_deviation",
        "@type": "http://www.w3.org/2001/XMLSchema#double"
      },
      "sample_size": {
        "@id": "http://trials.drugis.org/ontology#sample_size",
        "@type": "http://www.w3.org/2001/XMLSchema#integer"
      },
      "of_outcome": {
        "@id": "http://trials.drugis.org/ontology#of_outcome",
        "@type": "@id"
      },
      "of_moment": {
        "@id": "http://trials.drugis.org/ontology#of_moment",
        "@type": "@id"
      },
      "of_arm": {
        "@id": "http://trials.drugis.org/ontology#of_arm",
        "@type": "@id"
      },
      "mean": {
        "@id": "http://trials.drugis.org/ontology#mean",
        "@type": "http://www.w3.org/2001/XMLSchema#double"
      },
      "sameAs": {
        "@id": "http://www.w3.org/2002/07/owl#sameAs",
        "@type": "@id"
      },
      "measurementType": {
        "@id": "http://trials.drugis.org/ontology#measurementType",
        "@type": "@id"
      },
      "label": {
        "@id": "http://www.w3.org/2000/01/rdf-schema#label",
        "@type": "http://www.w3.org/2001/XMLSchema#string"
      },
      "comment": {
        "@id": "http://www.w3.org/2000/01/rdf-schema#comment",
        "@type": "http://www.w3.org/2001/XMLSchema#string"
      },
      "categoryList": {
        "@id": "http://trials.drugis.org/ontology#categoryList",
        "@type": "@id"
      },
      "has_outcome": {
        "@id": "http://trials.drugis.org/ontology#has_outcome",
        "@type": "@id"
      },
      "has_primary_epoch": {
        "@id": "http://trials.drugis.org/ontology#has_primary_epoch",
        "@type": "@id"
      },
      "has_publication": {
        "@id": "http://trials.drugis.org/ontology#has_publication",
        "@type": "@id"
      },
      "has_allocation": {
        "@id": "http://trials.drugis.org/ontology#has_allocation",
        "@type": "@id"
      },
      "has_objective": {
        "@id": "http://trials.drugis.org/ontology#has_objective",
        "@type": "@id"
      },
      "has_indication": {
        "@id": "http://trials.drugis.org/ontology#has_indication",
        "@type": "@id"
      },
      "has_activity": {
        "@id": "http://trials.drugis.org/ontology#has_activity",
        "@type": "@id"
      },
      "status": {
        "@id": "http://trials.drugis.org/ontology#status",
        "@type": "@id"
      },
      "has_arm": {
        "@id": "http://trials.drugis.org/ontology#has_arm",
        "@type": "@id"
      },
      "has_blinding": {
        "@id": "http://trials.drugis.org/ontology#has_blinding",
        "@type": "@id"
      },
      "has_epochs": {
        "@id": "http://trials.drugis.org/ontology#has_epochs",
        "@container": "@list"
      },
      "has_eligibility_criteria": {
        "@id": "http://trials.drugis.org/ontology#has_eligibility_criteria",
        "@type": "@id"
      },
      "has_number_of_centers": {
        "@id": "http://trials.drugis.org/ontology#has_number_of_centers",
        "@type": "http://www.w3.org/2001/XMLSchema#integer"
      },
      "rest": {
        "@id": "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest",
        "@type": "@id"
      },
      "first": {
        "@id": "http://www.w3.org/1999/02/22-rdf-syntax-ns#first",
        "@type": "@id"
      },
      "of_variable": {
        "@id": "http://trials.drugis.org/ontology#of_variable",
        "@type": "@id"
      },
      "is_measured_at": {
        "@id": "http://trials.drugis.org/ontology#is_measured_at",
        "@type": "@id"
      },
      "has_result_property": {
        "@id": "http://trials.drugis.org/ontology#has_result_property",
        "@type": "@id"
      },
      "count": {
        "@id": "http://trials.drugis.org/ontology#count",
        "@type": "http://www.w3.org/2001/XMLSchema#integer"
      },
      "has_drug_treatment": {
        "@id": "http://trials.drugis.org/ontology#has_drug_treatment",
        "@type": "@id"
      },
      "has_activity_application": {
        "@id": "http://trials.drugis.org/ontology#has_activity_application",
        "@type": "@id"
      },
      "category_count": {
        "@id": "http://trials.drugis.org/ontology#category_count",
        "@type": "@id"
      },
      "duration": {
        "@id": "http://trials.drugis.org/ontology#duration",
        "@type": "http://www.w3.org/2001/XMLSchema#duration"
      },
      "unit": {
        "@id": "http://trials.drugis.org/ontology#unit",
        "@type": "@id"
      },
      "dosingPeriodicity": {
        "@id": "http://trials.drugis.org/ontology#dosingPeriodicity",
        "@type": "http://www.w3.org/2001/XMLSchema#duration"
      },
      "value": {
        "@id": "http://www.w3.org/1999/02/22-rdf-syntax-ns#value",
        "@type": "http://www.w3.org/2001/XMLSchema#double"
      },
      "category": {
        "@id": "http://trials.drugis.org/ontology#category",
        "@type": "http://www.w3.org/2001/XMLSchema#string"
      },
      "has_id": {
        "@id": "http://trials.drugis.org/ontology#has_id",
        "@type": "@id"
      },
      "time_offset": {
        "@id": "http://trials.drugis.org/ontology#time_offset",
        "@type": "http://www.w3.org/2001/XMLSchema#duration"
      },
      "relative_to_epoch": {
        "@id": "http://trials.drugis.org/ontology#relative_to_epoch",
        "@type": "@id"
      },
      "relative_to_anchor": {
        "@id": "http://trials.drugis.org/ontology#relative_to_anchor",
        "@type": "@id"
      },
      "applied_to_arm": {
        "@id": "http://trials.drugis.org/ontology#applied_to_arm",
        "@type": "@id"
      },
      "applied_in_epoch": {
        "@id": "http://trials.drugis.org/ontology#applied_in_epoch",
        "@type": "@id"
      },
      "participants_starting": {
        "@id": "http://trials.drugis.org/ontology#participants_starting",
        "@type": "http://www.w3.org/2001/XMLSchema#integer"
      },
      "in_epoch": {
        "@id": "http://trials.drugis.org/ontology#in_epoch",
        "@type": "@id"
      },
      'treatment_dose': {
        '@id': 'http://trials.drugis.org/ontology#treatment_dose',
        '@type': '@id'
      },
      "treatment_min_dose": {
        "@id": "http://trials.drugis.org/ontology#treatment_min_dose",
        "@type": "@id"
      },
      "treatment_max_dose": {
        "@id": "http://trials.drugis.org/ontology#treatment_max_dose",
        "@type": "@id"
      },
      "treatment_has_drug": {
        "@id": "http://trials.drugis.org/ontology#treatment_has_drug",
        "@type": "@id"
      },
      "conversionMultiplier": {
        "@id": "http://qudt.org/schema/qudt#conversionMultiplier",
        "@type": "http://www.w3.org/2001/XMLSchema#double"
      },
      "ontology": "http://trials.drugis.org/ontology#"
    };

    var studyDataQuery = SparqlResource.get('queryStudyData.sparql');

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchStudyUri, query).then(function() {
          modified = true;
        });
      });
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchStudyUri, query);
      });
    }

    function createEmptyStudy(study, userUid, datasetUid) {
      var uuid = UUIDService.generate();
      var emptyStudy = {
        '@graph': [{
          '@id': 'http://trials.drugis.org/studies/' + uuid,
          '@type': 'ontology:Study',
          comment: study.comment,
          label: study.label,
          has_activity: [],
          has_arm: [],
          has_eligibility_criteria: [],
          has_epochs: [],
          has_indication: [],
          has_objective: [],
          has_outcome: [],
          has_publication: []
        }],
        '@context': context
      }

      var newVersionDefer = $q.defer();

      GraphResource.putJson({
        userUid: userUid,
        datasetUUID: datasetUid,
        graphUuid: uuid,
        commitTitle: 'Initial study creation: ' + study.label
      }, emptyStudy, function(value, responseHeaders) {
          var newVersion = responseHeaders('X-EventSource-Version');
          newVersion = newVersion.split('/')[4];
          newVersionDefer.resolve(newVersion);
      }, function(error) {
        console.error('error' + error);
      });

      return newVersionDefer.promise;
    }

    function queryStudyData() {
      return loadDefer.promise.then(function() {
        return studyDataQuery.then(function(query) {
          return RemoteRdfStoreService.executeQuery(scratchStudyUri, query).then(function(results) {
            return results[0];
          });
        });
      });
    }

    function loadStore(data) {
      return RemoteRdfStoreService.create(graphPrefix).then(function(graphUri) {
        scratchStudyUri = graphUri;
        return RemoteRdfStoreService.load(scratchStudyUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function getGraph() {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.getGraph(scratchStudyUri);
      });
    }

    function isStudyModified() {
      return modified;
    }

    function studySaved() {
      modified = false;
    }

    function getStudyUUID() {
      return $filter('stripFrontFilter')(scratchStudyUri, graphPrefix);
    }

    function reset() {
      loadDefer = $q.defer();
      modified = false;
    }

    function fillTemplate(template, study) {
      return template.replace(/\$studyUuid/g, study.uuid)
        .replace(/\$label/g, study.label)
        .replace(/\$comment/g, SanitizeService.sanitizeStringLiteral(study.comment));
    }

    function loadJson(jsonPromise) {
      studyJsonPromise = jsonPromise;
    }

    function getGraphAndContext() {
      return studyJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    function getJsonGraph() {
      return studyJsonPromise.then(function(graph) {
        return graph['@graph'];
      });
    }

    function saveJsonGraph(newGraph) {
      return studyJsonPromise.then(function(jsonLd) {
        jsonLd['@graph'] = newGraph;
        modified = true;
      });
    }

    function getStudy() {
      return studyJsonPromise.then(function(graph) {
        return _.find(graph['@graph'], function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
      });
    }

    function save(study) {
      return studyJsonPromise.then(function(jsonLd) {
        _.remove(jsonLd['@graph'], function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        jsonLd['@graph'].push(study);
        modified = true;
      });
    }

    return {
      loadStore: loadStore,
      queryStudyData: queryStudyData,
      createEmptyStudy: createEmptyStudy,
      getGraph: getGraph,
      doModifyingQuery: doModifyingQuery,
      doNonModifyingQuery: doNonModifyingQuery,
      isStudyModified: isStudyModified,
      studySaved: studySaved,
      getStudyUUID: getStudyUUID,
      reset: reset,
      loadJson: loadJson,
      getStudy: getStudy,
      save: save,
      getJsonGraph: getJsonGraph,
      saveJsonGraph: saveJsonGraph,
      getGraphAndContext: getGraphAndContext
    };
  };

  return dependencies.concat(StudyService);
});