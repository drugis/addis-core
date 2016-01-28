'use strict';
define([],
  function() {
    var dependencies = ['SparqlResource', 'StudyService', 'UUIDService'];
    var ResultsService = function(SparqlResource, StudyService, UUIDService) {

      var addResultValueRaw = SparqlResource.get('addResultValue.sparql');
      var updateResultValueQueryRaw = SparqlResource.get('updateResultValue.sparql');
      var queryResultsRaw = SparqlResource.get('queryResults.sparql');
      var deleteResultValueRaw = SparqlResource.get('deleteResultValue.sparql');
      var cleanUpMeasurementsTemplate = SparqlResource.get('cleanUpMeasurements.sparql');

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

      function createTypedInputColumn(inputColumn) {
        var result = angular.copy(inputColumn);
        if (inputColumn.value) {
          result.value = '"' + inputColumn.value + '"' + '^^' + inputColumn.dataType;
        }
        return result;
      }

      function updateResultValue(row, inputColumn) {
        var typedInputColumn = createTypedInputColumn(inputColumn);
        if (!row.uri) {
          row.uri = 'http://trials.drugis.org/instances/' + UUIDService.generate();
          return addNewResultValue(row, typedInputColumn);
        } else {
          if (typedInputColumn.value === null) {
            return deleteExistingValue(row, typedInputColumn);
          } else {
            return updateExistingResultValue(row, typedInputColumn);
          }
        }
      }

      function queryResults(variableUri) {
        return queryResultsRaw.then(function(template) {
          var query = template.replace(/\$outcomeUri/g, variableUri);
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function cleanUpMeasurements() {
        return cleanUpMeasurementsTemplate.then(function(template) {
          return StudyService.doModifyingQuery(template);
        });
      }

      function fillTemplate(template, row, inputColumn) {
        return template
          .replace(/\$resultUri/g, row.uri)
          .replace(/\$outcomeUri/g, row.variable.uri)
          .replace(/\$armUri/g, row.arm.armURI)
          .replace(/\$momentUri/g, row.measurementMoment.uri)
          .replace(/\$valueType/g, inputColumn.valueName)
          .replace(/\$actualValue/g, inputColumn.value);
      }

      return {
        updateResultValue: updateResultValue,
        queryResults: queryResults,
        cleanUpMeasurements: cleanUpMeasurements
      };
    };
    return dependencies.concat(ResultsService);
  });