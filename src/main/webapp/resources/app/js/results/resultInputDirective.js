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
        scope.updateValue = updateValue;

        function updateValue(row, cell) {
          if (ResultsTableService.isValidValue(cell)) {
            cell.isInValidValue = false;
            ResultsService.updateResultValue(row, cell).then(function(result) {
              row.uri = result;
            });
          } else {
            cell.isInValidValue = true;
          }
        }
      }
    };
  };

  return dependencies.concat(resultInputDirective);
});
