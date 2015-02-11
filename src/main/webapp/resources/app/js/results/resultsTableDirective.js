'use strict';
define([], function() {
  var dependencies = ['$q', '$stateParams', 'ResultsTableService', 'ArmService', 'MeasurementMomentService'];

  var resultsTableDirective = function($q, $stateParams, ResultsTableService, ArmService, MeasurementMomentService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableDirective.html',
      scope: {
        variable: '='
      },
      link: function(scope) {
        var arms, measurementMoments;
        var armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
          arms = result;
        });
        var measurementMomentsPromise = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
          measurementMoments = result;
        });;

        // $q.all([armsPromise, measurementMomentsPromise]).then(function() {
        //   scope.inputRows = ResultsTableService.createInputRows(scope.variable, arms, measurementMoments);
        // });

        scope.variable= {
          label: 'headache'
        }

        scope.inputRows = [
          {
            measurementMoment: {
              label: 'start of mainphase'
            },
            arm: {
              label: 'parox'
            },
            mean: 10,
            sd: 3.5,
            n: 30
          },          {
            measurementMoment: {
              label: 'start of mainphase'
            },
            arm: {
              label: 'sertra'
            },
            mean: 10,
            sd: 3.5,
            n: 30
          },          {
            measurementMoment: {
              label: 'end of mainphase'
            },
            arm: {
              label: 'parox'
            },
            mean: 10,
            sd: 3.5,
            n: 30
          },
          {
            measurementMoment: {
              label: 'end of mainphase'
            },
            arm: {
              label: 'sertra'
            },
            mean: 10,
            sd: 3.5,
            n: 30
          }];
          scope.inputRows.nArms = 2;
      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
