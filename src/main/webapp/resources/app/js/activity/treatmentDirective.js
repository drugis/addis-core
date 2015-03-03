'use strict';
define([], function() {
  var dependencies = ['$stateParams', 'DrugService', 'UnitService', 'UUIDService'];
  var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

  var TreatmentDirective = function($stateParams, DrugService, UnitService, UUIDService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/activity/treatmentDirective.html',
      link: function(scope) {

        scope.treatment = {};
        scope.treatment.dosingPeriodicity = 'P1D';
        scope.treatment.treatmentDoseType = 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment';

        DrugService.queryItems($stateParams.studyUUID).then(function(result){
          scope.drugs = result;
        });

        UnitService.queryItems($stateParams.studyUUID).then(function(result){
          scope.doseUnits = result;
        });

        function reset() {
          scope.treatment = {
            dosingPeriodicity: 'P1D',
            treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment'
          };
        }

        scope.cancelAddDrug = function() {
          scope.treatment = {};
          scope.treatmentDirective.isVisible = false;
        }

        scope.isValidTreatment = function() {
          var baseValid = scope.treatment.drug.label && scope.treatment.doseUnit.label && scope.treatment.dosingPeriodicity;
          if(scope.treatment.dinges === 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment') {
            return baseValid && scope.treatment.fixedValue !== undefined;
          } else if (scope.treatment.dinges === 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment') {
            return baseValid && scope.treatment.minValue !== undefined && scope.treatment.maxValue !== undefined;
          }
          return false;
        }


        scope.addTreatment = function(treatment) {
          var newTreatment = angular.copy(treatment);

          if(!scope.itemScratch.treatments) {
            scope.itemScratch.treatments = [];
          }

          if(angular.isString(newTreatment.drug)) {
              newTreatment.drug = {
                uri: INSTANCE_PREFIX + UUIDService.generate(),
                label: treatment.drug
              };
              scope.drugs.push(newTreatment.drug);
          }

          if(angular.isString(newTreatment.doseUnit)) {
            newTreatment.doseUnit = {
              uri: INSTANCE_PREFIX + UUIDService.generate(),
              label: newTreatment.doseUnit
            };
            scope.doseUnits.push(newTreatment.doseUnit);
          }

          reset();
          scope.treatmentAdded(newTreatment);
        }

      }

    };
  };

  return dependencies.concat(TreatmentDirective);
});
