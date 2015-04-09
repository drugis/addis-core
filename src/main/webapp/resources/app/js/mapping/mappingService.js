'use strict';
define([], function() {
  var dependencies = ['SparqlResource', 'StudyService'];

  var MappingService = function(SparqlResource, StudyService) {

    var addMappingQuery = SparqlResource.get('addMapping.sparql');

    function queryItems(datasetUuid) {
      return [{
        label: 'test'
      }];
    }

    function addItem(datasetConcept, studyConcept) {
      return addMappingQuery.then(template) {
        var query = fillInTemplate(template, datasetConcept, studyConcept);
        return StudyService.doModifyingQuery(query);
      }
    }

    return {
      queryItems: queryItems,
      addItem: addItem
    };

  };
  return dependencies.concat(MappingService);
});
