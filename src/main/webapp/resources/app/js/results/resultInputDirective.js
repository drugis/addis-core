'use strict';
define([], function() {
  var dependencies = ['ResultsTableService', 'ResultsService'];

  var resultInputDirective = function(ResultsTableService, ResultsService) {
    return {
      restrict: 'E',
      templateUrl: './resultInputDirective.html',
      scope: {
        row: '=',
        column: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        scope.updateValue = function(row, column) {
          if (ResultsTableService.isValidValue(column)) {
            column.isInValidValue = false;
            ResultsService.updateResultValue(row, column).then(function(result){
              row.uri = result;
            });
          } else {
            column.isInValidValue = true;
          }
        };

      }
    };
  };

  return dependencies.concat(resultInputDirective);
});
