'use strict';
define([], function() {
  var dependencies = ['SparqlResource', 'StudyService'];

  var MappingService = function(SparqlResource, StudyService) {

    var addMappingQuery = SparqlResource.get('addMapping.sparql');

    function fillInTemplate(template, datasetConcept, studyConcept) {
      return '';
    }

    function queryItems(datasetUuid) {
      return [{
        label: 'test'
      }];
    }

    function createMapping(studyConcept, datasetConcept) {
      return {
        studyConcept: studyConcept,
        datasetConcept: datasetConcept
      };
    }

    function addItem(datasetConcept, studyConcept) {
      return addMappingQuery.then(function(template) {
        var query = fillInTemplate(template, datasetConcept, studyConcept);
        return StudyService.doModifyingQuery(query);
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      createMapping: createMapping
    };

  };
  return dependencies.concat(MappingService);
});
