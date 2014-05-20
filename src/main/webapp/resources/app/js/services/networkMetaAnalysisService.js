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
        return trialDataIntervention.drugId === trialDataArm.drugId;
      });
    }

    function findMeasurementValue(measurements, measurementAttribute, valueType) {
      var measurement = _.find(measurements, function(measurement) {
        return measurement.measurementAttribute === measurementAttribute;
      });
      return measurement !== undefined ? measurement[valueType] : null;
    }

    function sortTableByStudyAndIntervention(table) {
      // sort table by studies and interventions
      var tableRowComparator = function(left, right) {
        if (left.study > right.study) {
          return 1;
        } else if (left.study < right.study) {
          return -1;
        }
        //studies equal then order by intervention, placing unmapped interventions last
        if (left.intervention === 'unmatched') {
          return 1;
        }
        if (right.intervention === 'unmatched') {
          return -1;
        }
        if (left.intervention > right.intervention) {
          return 1;
        } else if (left.intervention < right.intervention) {
          return -1;
        }
        return 0;
      };

      table.sort(tableRowComparator);
      return table;
    }

    function addRenderingHintsToTable(table) {
      // add information to render the table
      var currentStudy = 'null';
      var currentInterventionRow = {
        intervention: null
      };
      angular.forEach(table, function(row) {

        if (row.intervention !== currentInterventionRow.intervention || row.intervention === 'unmatched') {
          row.firstInterventionRow = true;
          currentInterventionRow = row;
          currentInterventionRow.interventionRowSpan = 0;
        }

        if (row.study !== currentStudy) {
          row.firstStudyRow = true;
          currentStudy = row.study;
          currentInterventionRow = row;
          currentInterventionRow.interventionRowSpan = 0;
        }

        ++currentInterventionRow.interventionRowSpan;
      });
      return table;
    }

    function buildTableFromTrialData(data, interventions) {
      var rows = [];
      angular.forEach(data.trialDataStudies, function(study) {
        angular.forEach(study.trialDataArms, function(trialDataArm) {
          var trialDataIntervention = mapTrialDataArmToIntervention(trialDataArm, study.trialDataInterventions);
          var row = {};
          row.study = study.name;
          row.studyRowSpan = study.trialDataArms.length;
          row.intervention = trialDataIntervention ? resolveInterventionName(trialDataIntervention, interventions) : 'unmatched';
          row.arm = trialDataArm.name;
          row.rate = findMeasurementValue(trialDataArm.measurements, 'rate', 'integerValue');
          row.mu = findMeasurementValue(trialDataArm.measurements, 'mean', 'realValue');
          row.sigma = findMeasurementValue(trialDataArm.measurements, 'standard deviation', 'realValue');
          row.sampleSize = findMeasurementValue(trialDataArm.measurements, 'sample size', 'integerValue');

          rows.push(row);
        });
      });
      return rows;
    }

    return {
      transformTrialDataToTableRows: function(trialData) {
        var transformTrialDataToTableRowsDefer = $q.defer();

        InterventionResource
          .query($stateParams)
          .$promise
          .then(function(interventions) {
            var tableRows = buildTableFromTrialData(trialData, interventions);

            tableRows = sortTableByStudyAndIntervention(tableRows);
            tableRows = addRenderingHintsToTable(tableRows);
            transformTrialDataToTableRowsDefer.resolve(tableRows);
          });
        return transformTrialDataToTableRowsDefer.promise;
      }
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});