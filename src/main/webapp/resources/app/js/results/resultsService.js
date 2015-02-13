'use strict';
define([],
  function() {
    var dependencies = ['SparqlResource', 'StudyService', 'UUIDService'];
    var ResultsService = function(SparqlResource, StudyService, UUIDService) {

      var addResultValueRaw = SparqlResource.get('addResultValue.sparql');
      var updateResultValueQueryRaw = SparqlResource.get('updateResultValue.sparql');
      var queryResultsRaw = SparqlResource.get('queryResults.sparql');

      function addNewResultValue(row, valueName, inputColumn) {
        return addResultValueRaw.then(function(query) {
          var substituted = fillTemplate(query, row, valueName, inputColumn);
          return StudyService.doModifyingQuery(substituted);
        });
      };

      function updateResultExsistingResultValue(row, valueName, inputColumn) {
        return updateResultValueQueryRaw.then(function(query) {
          var substituted = fillTemplate(query, row, valueName, inputColumn);
          return StudyService.doModifyingQuery(substituted);
        });
      }

      function updateResultValue(row, valueName, inputColumn) {
        if (!row.uri) {
          row.uri = 'http://trials.drugis.org/instances/' + UUIDService.generate();
          return addNewResultValue(row, valueName, inputColumn);
        } else {
          return updateResultExsistingResultValue(row, valueName, inputColumn);
        }
      }

      function fillTemplate(template, row, valueName, inputColumn) {
        return template
          .replace(/\$resultUuid/g, row.uuid)
          .replace(/\$outcomeUri/g, row.variable.uri)
          .replace(/\$armUri/g, row.arm.uri)
          .replace(/\$momentUri/g, row.measurementMoment.uri)
          .replace(/\$valueType/g, valueName)
          .replace(/\$actualValue/g, inputColumn.value);
      }

      return {
        updateResultValue: updateResultValue
      };
    };
    return dependencies.concat(ResultsService);
  });
