'use strict';
define([],
  function() {
    var dependencies = ['SparqlResource', 'StudyService', 'UUIDService'];
    var ResultsService = function(SparqlResource, StudyService, UUIDService) {
      var updateResultValueQueryRaw = SparqlResource.get('updateResultValue');

      function updateResultValue(valueName, row) {
        return updateResultValueQueryRaw.then(function(query) {
          if (!row.uuid) {
            row.uuid = UUIDService.generate();
          }
          var substituted = query
            .replace(/\$resultUuid/g, row.uuid)
            .replace(/\$outcomeUri/g, row.variable.uri)
            .replace(/\$armUri/g, row.arm.uri)
            .replace(/\$momentUri/g, row.measurementMoment.uri)
            .replace(/\$valueType/g, valueName)
            .replace(/\$actualValue/g, row[valueName]);
          // ALL the substitutions
          return StudyService.doModifyingQuery(substituted);
        });
      }

      return {
        updateResultValue: updateResultValue
      };
    };
    return dependencies.concat(ResultsService);
  });
