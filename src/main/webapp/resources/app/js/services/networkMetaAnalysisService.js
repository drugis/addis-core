'use strict';
define(['angular'], function() {
  var dependencies = ['$stateParams', '$q', 'InterventionResource'];
  var NetworkMetaAnalysisService = function($stateParams, $q, InterventionResource) {

    function resolveInterventionName(trialDataIntervention, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === trialDataIntervention.uri;
      }).name;
    }

    function mapTrialDataArmToIntervention(trialDataArm, trialDataInterventions) {
       return _.find(trialDataInterventions, function(trialDataIntervention) {
          return trialDataIntervention.drugId === trialDataArm.drugId
       });
    }

    function findMeasurementValue(measurements, measurementAttribute, valueType) {
      var measurement = _.find(measurements, function(measurement){
        return measurement.measurementAttribute === measurementAttribute;
      });
      return measurement !== undefined ? measurement[valueType] : null;
    };

    return {
      transformTrialDataToTableRows: function(trialData) {
        var tableRows = [],
          transformTrialDataToTableRowsDefer = $q.defer();

        InterventionResource
          .query($stateParams)
          .$promise
          .then(function(interventions) {

            // build the table
            angular.forEach(trialData.trialDataStudies, function(study) {
              angular.forEach(study.trialDataArms, function(trialDataArm, index) {
                var trialDataIntervention = mapTrialDataArmToIntervention(trialDataArm, study.trialDataInterventions);
                var row = {};
                if (index === 0) {
                  row.study = study.name;
                  row.studyRowSpan = study.trialDataArms.length;
                }

                row.intervention = trialDataIntervention ? resolveInterventionName(trialDataIntervention, interventions) : 'unmatched';     
                row.arm = trialDataArm.name
                row.rate = findMeasurementValue(trialDataArm.measurements, 'rate', 'integerValue');
                row.mu = findMeasurementValue(trialDataArm.measurements, 'mean', 'realValue');
                row.sigma = findMeasurementValue(trialDataArm.measurements, 'standard deviation', 'realValue');
                row.sampleSize = findMeasurementValue(trialDataArm.measurements, 'sample size', 'integerValue');

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