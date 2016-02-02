'use strict';
define([],
  function() {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'callback',
      'actionType',
      'ActivityService',
      'DrugService',
      'UnitService'
    ];
    var ActivityController = function($scope, $modalInstance, callback, actionType, ActivityService, DrugService, UnitService) {

      $scope.actionType = actionType;
      $scope.activityTypeOptions = _.values(ActivityService.ACTIVITY_TYPE_OPTIONS);

      $scope.treatmentDirective = {
        isVisible : false
      };

      $scope.addDrugClicked = function() {
        $scope.treatmentDirective.isVisible = true;
      }

      $scope.treatmentAdded = function(treatment) {
        $scope.itemScratch.treatments.push(treatment);
        $scope.treatmentDirective.isVisible = false;
      }

      $scope.addItem = function() {
        ActivityService.addItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create activity');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.editItem = function() {
        ActivityService.editItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to edit activity');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      }

      if ($scope.actionType === 'Add') {
        $scope.itemScratch = {};
        $scope.itemScratch.treatment = {};
        $scope.itemScratch.doseType = 'FixedDoseDrugTreatment';
        $scope.commit = $scope.addItem; // set the function to be called when the form is submitted
      } else {
        $scope.itemScratch = angular.copy($scope.item);
        // select from the options map as ng-select works by reference
        $scope.itemScratch.activityType = ActivityService.ACTIVITY_TYPE_OPTIONS[$scope.itemScratch.activityType.uri];
        $scope.commit = $scope.editItem;
      }

      $scope.$watch(function() {
        return angular.element('.scrollable-wrapper').parent().parent().height();
      }, function(oldValue, newValue){
          //console.log('recalculate , old = ' + oldValue, ' new = ' + newValue);
          $scope.showScrolbarIfNessesary();
      });

      $scope.showScrolbarIfNessesary = function() {
        var offsetString = angular.element('.reveal-modal').css('top');
        var offset = parseInt(offsetString , 10); // remove px part
        var viewPortHeight = angular.element('html').height();
        var scrollableWrapperElement = angular.element('.scrollable-wrapper');
        var wrapperHeight = scrollableWrapperElement.parent().parent().height();
        var maxWrapperHeight = viewPortHeight - offset;

        if(wrapperHeight > maxWrapperHeight) {
          scrollableWrapperElement.css('max-height', viewPortHeight - offset);
        } else {
          scrollableWrapperElement.css('max-height', viewPortHeight + offset);
        }
      }

    };
    return dependencies.concat(ActivityController);
  });
