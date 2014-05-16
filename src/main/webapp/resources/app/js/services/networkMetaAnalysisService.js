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

    function findRateMeasurementValue(measurements) {
      var rateMeasurement = _.find(measurements, function(measurement){
        return measurement.measurementAttribute === 'rate';
      });
      return rateMeasurement ? rateMeasurement.integerValue : null;
    }

    function findSampleSizeMeasurementValue(measurements) {
      var sampleSizeMeasurement = _.find(measurements, function(measurement){
        return measurement.measurementAttribute === 'sample size';
      });
      return sampleSizeMeasurement ? sampleSizeMeasurement.integerValue : null;
    }

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
                row.rate = findRateMeasurementValue(trialDataArm.measurements);
                row.sampleSize = findSampleSizeMeasurementValue(trialDataArm.measurements);

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