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
        scope.referenceChanged = referenceChanged;

        scope.inputType = determineInputType();

        function determineInputType() {
          var inputTypes = {
            '<http://www.w3.org/2001/XMLSchema#boolean>': 'reference',
            '<http://www.w3.org/2001/XMLSchema#integer>': 'number',
            '<http://www.w3.org/2001/XMLSchema#double>': 'number'
          };
          return inputTypes[scope.column.dataType];
        }

        function referenceChanged(row, column) {
          row.isReference = true;
          var activityUri = row.group.armURI ? row.group.armURI : row.group.groupUri;
          ResultsService.updateResultValue(row, column).then(function(result) {
            row.uri = result;
            scope.$emit('referenceRowChanged', { uri: activityUri });
          });
        }

        function updateValue(row, column) {
          if (ResultsTableService.isValidValue(column)) {
            column.isInValidValue = false;
            ResultsService.updateResultValue(row, column).then(function(result) {
              row.uri = result;
            });
          } else {
            column.isInValidValue = true;
          }
        }
      }
    };
  };

  return dependencies.concat(resultInputDirective);
});
