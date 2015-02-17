'use strict';
define([],
  function() {
    var dependencies = ['SparqlResource', 'StudyService', 'UUIDService'];
    var ResultsService = function(SparqlResource, StudyService, UUIDService) {

      var addResultValueRaw = SparqlResource.get('addResultValue.sparql');
      var updateResultValueQueryRaw = SparqlResource.get('updateResultValue.sparql');
      var queryResultsRaw = SparqlResource.get('queryResults.sparql');
      var deleteResultValueRaw = SparqlResource.get('deleteResultValue.sparql');

      function addNewResultValue(row, inputColumn) {
        return addResultValueRaw.then(function(query) {
          var substituted = fillTemplate(query, row, inputColumn);
          return StudyService.doModifyingQuery(substituted);
        });
      };

      function updateExistingResultValue(row, inputColumn) {
        return updateResultValueQueryRaw.then(function(query) {
          var substituted = fillTemplate(query, row, inputColumn);
          return StudyService.doModifyingQuery(substituted);
        });
      }

      function deleteExistingValue(row, inputColumn) {
        return performQueryWhenLoaded(deleteResultValueRaw, row, inputColumn);
      }

      function performQueryWhenLoaded(queryPromise, row, inputColumn) {
        return queryPromise.then(function(query) {
          var substituted = fillTemplate(query, row, inputColumn);
          return StudyService.doModifyingQuery(substituted);
        });
      }

      function updateResultValue(row, inputColumn) {
        if (!row.uri) {
          row.uri = 'http://trials.drugis.org/instances/' + UUIDService.generate();
          return addNewResultValue(row, inputColumn);
        } else {
          if (inputColumn.value === null) {
            return deleteExistingValue(row, inputColumn);
          } else {
            return updateExistingResultValue(row, inputColumn);
          }
        }
      }

      function fillTemplate(template, row, inputColumn) {
        return template
          .replace(/\$resultUri/g, row.uri)
          .replace(/\$outcomeUri/g, row.variable.uri)
          .replace(/\$armUri/g, row.arm.uri)
          .replace(/\$momentUri/g, row.measurementMoment.uri)
          .replace(/\$valueType/g, inputColumn.valueName)
          .replace(/\$actualValue/g, inputColumn.value);
      }

      return {
        updateResultValue: updateResultValue
      };
    };
    return dependencies.concat(ResultsService);
  });
