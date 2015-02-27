'use strict';
define([], function() {
  var dependencies = ['$stateParams', 'DrugService', 'UnitService'];

  var TreatmentDirective = function($stateParams, DrugService, UnitService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/activity/treatmentDirective.html',
      link: function(scope) {

        scope.treatment = {};

        DrugService.queryItems($stateParams.studyUUID).then(function(result){
          scope.drugs = result;
        });

        UnitService.queryItems($stateParams.studyUUID).then(function(result){
          scope.doseUnits = result;
        });

        scope.cancelAddDrug = function() {
          scope.treatment = {};
          scope.treatmentDirective.isVisible = false;
        }

      }

    };
  };

  return dependencies.concat(TreatmentDirective);
});
