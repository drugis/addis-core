'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelService',
    'PataviService', 'RelativeEffectsTableService', 'CacheService'
  ];
  var TreatmentEffectsDirective = function($stateParams, $q, ModelService,
    PataviService, RelativeEffectsTableService, CacheService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        baselineTreatmentId: '=',
        regressionLevel: '=',
        sortingType: '='
      },
      templateUrl: 'app/js/project/report/treatmentEffects/treatmentEffectsTemplate.html',

      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }

        var problemPromise = CacheService.getProblem($stateParams.projectId, scope.analysisId);
        var modelPromise = CacheService.getModel($stateParams.projectId, scope.analysisId, scope.modelId);

        $q.all([problemPromise, modelPromise]).then(function(values) {
          var problem = values[0];
          var model = values[1];

          if (problem.treatments && problem.treatments.length > 0) {
            scope.selectedBaseline = _.find(problem.treatments, ['id', scope.baselineTreatmentId]);
          }

          getResults(model).then(function(results) {
            scope.treatmentEffectsRows = _.map(results.relativeEffects, function(relativeEffect, key) {
              return {
                level: key,
                row: RelativeEffectsTableService.buildRow(relativeEffect, problem.treatments, scope.selectedBaseline.id, results.logScale)
              };
            });

            if (scope.regressionLevel !== undefined) {
              scope.treatmentEffects = _.find(scope.treatmentEffectsRows, ['level', scope.regressionLevel.toString()]);
            } else {
              scope.treatmentEffects = scope.treatmentEffectsRows[0];
            }

            if (scope.sortingType === 'point-estimate') {
              scope.treatmentEffects.row = _.sortBy(scope.treatmentEffects.row, 'median');
            } else {
              scope.treatmentEffects.row = _.sortBy(scope.treatmentEffects.row, 'name');
            }
          });
        });
      }
    };
  };
  return dependencies.concat(TreatmentEffectsDirective);
});
