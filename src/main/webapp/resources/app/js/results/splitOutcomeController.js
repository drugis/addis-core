'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'ResultsService', 'variableType' ,'outcome', 'nonConformantMeasurementsMap', 'callback'];
    var SplitOutcomeController = function($scope, $state, $modalInstance, ResultsService, variableType, outcome, nonConformantMeasurementsMap, callback) {

      $scope.outcome = outcome;
      $scope.nonConformantMeasurementsMap = nonConformantMeasurementsMap;

      $scope.splitOutcome = function(targetName) {
        var includedRows = _.filter(_.values($scope.nonConformantMeasurementsMap), 'toMove');
        var urisTomove = _.reduce(includedRows, function(uris, row) {
          var urisForRow = _.reduce(_.values(row), function(accum, group){
            return accum.concat(_.map(group.results, 'instance'));
          }, []);
          return uris.concat(urisForRow);
        }, []);
        ResultsService.moveToNewOutcome(variableType, targetName, $scope.outcome, urisTomove).then(function(){
          callback();
          $modalInstance.close();
        });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(SplitOutcomeController);
  });
