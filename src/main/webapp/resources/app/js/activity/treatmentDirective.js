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
        scope.treatment.treatmentDoseType = 'ontology:FixedDoseDrugTreatment';

        DrugService.queryItems($stateParams.studyUUID).then(function(result){
          scope.drugs = result;
        });

        UnitService.queryItems($stateParams.studyUUID).then(function(result){
          scope.doseUnits = result;
        });

        function reset() {
          scope.treatment = {
            dosingPeriodicity: 'P1D',
            treatmentDoseType: 'ontology:FixedDoseDrugTreatment'
          };
        }

        scope.cancelAddDrug = function() {
          scope.treatment = {};
          scope.treatmentDirective.isVisible = false;
        };

        function isDefinedNumber(number) {
          return typeof number === 'number';
        }

        function isCompleteTypeaheadValue(obj) {
          // the typeahead input can either be a string (if new value) or an object (if one is picked from existing options)
          return (typeof obj === 'string' && obj.length > 0)
            || (obj && obj.label)
        }

        scope.isValidTreatment = function() {
          var baseValid = isCompleteTypeaheadValue(scope.treatment.drug) &&
            isCompleteTypeaheadValue(scope.treatment.doseUnit) && scope.treatment.dosingPeriodicity &&
            scope.treatment.dosingPeriodicity !== 'PnullD';
          if(scope.treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment') {
            return baseValid && isDefinedNumber(scope.treatment.fixedValue);
          } else if (scope.treatment.treatmentDoseType === 'ontology:TitratedDoseDrugTreatment') {
            return baseValid && isDefinedNumber(scope.treatment.minValue) && isDefinedNumber(scope.treatment.maxValue);
          }
          return false;
        };


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
        };

      }

    };
  };

  return dependencies.concat(TreatmentDirective);
});
