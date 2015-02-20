'use strict';
define([], function() {
  var dependencies = ['ResultsTableService', 'ResultsService'];

  var resultInputDirective = function(ResultsTableService, ResultsService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultInputDirective.html',
      scope: {
        row: '=',
        column: '=',
      },
      link: function(scope) {

        scope.updateValue = function(row, column) {
          if(ResultsTableService.isValidValue(column)) {
            column.isInValidValue = false;
            ResultsService.updateResultValue(row, column);
          } else{
            column.isInValidValue = true;
          }
        }
        
      }
    };
  };

  return dependencies.concat(resultInputDirective);
});