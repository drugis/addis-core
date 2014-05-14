'use strict';
define(['angular'], function() {
  var dependencies = [];
  var NetworkMetaAnalysisService = function() {
    return {
      transformTrialDataToTableRows: function(studies) {
        var tableRows = [];
        angular.forEach(studies, function(study) {
          angular.forEach(study.trialDataInterventions, function(intervention, index) {
            var row = {};
            if (index === 0) {
              row.study = study.title;
              row.rowSpan = study.trialDataInterventions.length;
            }
            row.intervention = intervention.drugId;
            tableRows.push(row);
          });
        });
        return tableRows;
      }
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});