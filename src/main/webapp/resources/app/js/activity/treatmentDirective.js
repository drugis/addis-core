'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['$stateParams', 'DrugService', 'UnitService', 'UUIDService'];
  var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

  var TreatmentDirective = function($stateParams, DrugService, UnitService, UUIDService) {
    return {
      restrict: 'E',
      templateUrl: './treatmentDirective.html',
      link: function(scope) {

        scope.treatment = {};
        scope.treatment.dosingPeriodicity = 'P1D';
        scope.treatment.treatmentDoseType = 'ontology:FixedDoseDrugTreatment';
        scope.isValidTreatment = false;

        DrugService.queryItems($stateParams.studyUUID).then(function(result) {
          scope.drugs = result;
        });

        UnitService.queryItems($stateParams.studyUUID).then(function(result) {
          scope.doseUnits = result;
        });

        scope.drugChanged = drugChanged;
        scope.doseUnitChanged = doseUnitChanged;
        scope.cancelAddDrug = cancelAddDrug;
        scope.checkIsValidTreatment = checkIsValidTreatment;
        scope.addTreatment = addTreatment;

        function reset() {
          scope.treatment = {
            dosingPeriodicity: 'P1D',
            treatmentDoseType: 'ontology:FixedDoseDrugTreatment'
          };
        }

        function drugChanged(newValue) {
          if (newValue) {
            scope.treatment.drug = newValue.originalObject;
          }
          checkIsValidTreatment();
        }

        function doseUnitChanged(newValue) {
          if (newValue) {
            scope.treatment.doseUnit = newValue.originalObject;
          }
          checkIsValidTreatment();

        }

        function cancelAddDrug() {
          scope.treatment = {};
          scope.treatmentDirective.isVisible = false;
          scope.$broadcast('angucomplete-alt:clearInput');
        }

        function isDefinedNumber(number) {
          return typeof number === 'number';
        }

        function isCompleteTypeaheadValue(obj) {
          // the typeahead input can either be a string (if new value) or an object (if one is picked from existing options)
          return (typeof obj === 'string' && obj.length > 0) || (obj && obj.label);
        }

        function checkIsValidTreatment() {
          scope.isValidTreatment = false;
          var baseValid = isCompleteTypeaheadValue(scope.treatment.drug) &&
            isCompleteTypeaheadValue(scope.treatment.doseUnit) && scope.treatment.dosingPeriodicity &&
            scope.treatment.dosingPeriodicity !== 'PnullD';
          if (scope.treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment') {
            scope.isValidTreatment = baseValid && isDefinedNumber(scope.treatment.fixedValue);
          } else if (scope.treatment.treatmentDoseType === 'ontology:TitratedDoseDrugTreatment') {
            scope.isValidTreatment = baseValid && isDefinedNumber(scope.treatment.minValue) && isDefinedNumber(scope.treatment.maxValue);
          }
        }

        function createIfNotExists(newTreatment, propertyName) {
          if (angular.isString(newTreatment[propertyName])) {
            var existingItem = _.find(scope[propertyName + 's'], function(property) {
              return property.label.toLowerCase() === newTreatment[propertyName].toLowerCase();
            });
            if (!existingItem) {
              newTreatment[propertyName] = {
                uri: INSTANCE_PREFIX + UUIDService.generate(),
                label: newTreatment[propertyName]
              };
              scope[propertyName + 's'].push(newTreatment[propertyName]);
            } else {
              newTreatment[propertyName] = existingItem;
            }
          }
        }

        function addTreatment(treatment) {
          var newTreatment = _.cloneDeep(treatment);
          if (!scope.itemScratch.treatments) {
            scope.itemScratch.treatments = [];
          }
          createIfNotExists(newTreatment, 'drug');
          createIfNotExists(newTreatment, 'doseUnit');
          reset();
          scope.treatmentAdded(newTreatment); // function in activityController.js
          scope.$broadcast('angucomplete-alt:clearInput');
        }

      }

    };
  };

  return dependencies.concat(TreatmentDirective);
});
