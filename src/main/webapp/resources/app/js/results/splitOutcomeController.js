'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'ResultsService', 'variableType', 'outcome', 'nonConformantMeasurementsMap', 'callback'];
    var SplitOutcomeController = function($scope, $state, $modalInstance, ResultsService, variableType, outcome, nonConformantMeasurementsMap, callback) {

      $scope.outcome = outcome;
      $scope.nonConformantMeasurementsMap = nonConformantMeasurementsMap;
      $scope.labelChecked = labelChecked;
      $scope.splitOutcome = splitOutcome;
      $scope.cancel = cancel;

      function labelChecked() {
        $scope.numberOfRowsChecked = _.filter($scope.nonConformantMeasurementsMap, 'toMove').length;
      }

      function splitOutcome(targetName) {
        var includedRows = _.filter($scope.nonConformantMeasurementsMap, 'toMove');
        var urisTomove = _.reduce(includedRows, function(uris, row) {
          var urisForRow = _.reduce(_.values(row), function(accum, group) {
            return accum.concat(_.map(group.results, 'instance'));
          }, []);
          return uris.concat(urisForRow);
        }, []);
        ResultsService.moveToNewOutcome(variableType, targetName, $scope.outcome, urisTomove).then(function() {
          callback();
          $modalInstance.close();
        });
      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(SplitOutcomeController);
  });
