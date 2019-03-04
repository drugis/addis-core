'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$stateParams',
    'NetworkMetaAnalysisService'
  ];
  var AbsoluteEvidenceTable = function(
    $stateParams,
    NetworkMetaAnalysisService
  ) {
    return {
      restrict: 'E',
      templateUrl: './AbsoluteEvidenceTableDirective.html',
      scope: {
        rows: '=',
        editMode: '=',
        studies: '=',
        analysis: '=',
        project: '='
      },
      link: function(scope) {
        scope.doesInterventionHaveAmbiguousArms = doesInterventionHaveAmbiguousArms;
        scope.changeArmExclusion = changeArmExclusion;
        scope.changeMeasurementMoment = changeMeasurementMoment;

        scope.columns = [{
          label: 'subject with event',
          helpKey: 'count',
          dataKey: 'rate'
        }, {
          label: 'mean',
          helpKey: 'mean',
          dataKey: 'mu'
        }, {
          label: 'standard deviation',
          helpKey: 'standard-deviation',
          dataKey: 'sigma'
        }, {
          label: 'N',
          helpKey: 'sample-size',
          dataKey: 'sampleSize'
        }, {
          label: 'standard error',
          helpKey: 'standard-error',
          dataKey: 'stdErr'
        }, {
          label: 'exposure',
          helpKey: 'exposure',
          dataKey: 'exposure'
        }];

        scope.$watch('rows', refreshStuff);
        scope.userId = Number($stateParams.userUid);

        function refreshStuff(newValue) {
          if (newValue) {
            scope.momentSelections = NetworkMetaAnalysisService.buildMomentSelections(scope.studies, scope.analysis);
            scope.showColumn = NetworkMetaAnalysisService.getAbsoluteColumnsToShow(scope.rows);
            scope.missingValuesByStudy = NetworkMetaAnalysisService.buildMissingValueByStudy(scope.rows, scope.momentSelections);
          }
        }

        function doesInterventionHaveAmbiguousArms(interventionId, studyUri) {
          return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(interventionId, studyUri, scope.studies, scope.analysis);
        }

        function changeArmExclusion(row) {
          scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(row, scope.analysis);
          scope.$emit('armExclusionChanged');
        }

        function changeMeasurementMoment(newMeasurementMoment, row) {
          // always remove old inclusion for this study
          scope.analysis.includedMeasurementMoments = _.reject(scope.analysis.includedMeasurementMoments, ['study', row.studyUri]);

          if (!newMeasurementMoment.isDefault) {
            var newInclusion = {
              analysisId: scope.analysis.id,
              study: row.studyUri,
              measurementMoment: newMeasurementMoment.uri
            };
            scope.analysis.includedMeasurementMoments.push(newInclusion);
          }
          scope.$emit('saveAnalysisAndReload');
        }
      }
    };
  };
  return dependencies.concat(AbsoluteEvidenceTable);
});
