'use strict';
define(['angular'], function() {
  var dependencies = ['$stateParams', '$q', 'InterventionResource'];
  var NetworkMetaAnalysisService = function($stateParams, $q, InterventionResource) {

    function trialDataInterventionToIntervention(trialDataIntervention, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === trialDataIntervention.uri;
      });
    }

    return {
      transformTrialDataToTableRows: function(triallData) {
        var tableRows = [],
          transformTrialDataToTableRowsDefer = $q.defer();

        InterventionResource
          .query($stateParams)
          .$promise
          .then(function(interventions) {
            angular.forEach(triallData.studies, function(study) {
              angular.forEach(study.trialDataInterventions, function(trialDataIntervention, index) {
                var row = {};
                if (index === 0) {
                  row.study = study.title;
                  row.rowSpan = study.trialDataInterventions.length;
                }
                row.intervention = trialDataInterventionToIntervention(trialDataIntervention, interventions).name;
                tableRows.push(row);
              });
            });
            transformTrialDataToTableRowsDefer.resolve(tableRows);
          });
        return transformTrialDataToTableRowsDefer.promise;
      }
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});