'use strict';
define(['lodash', 'angular'],
  function(_, angular) {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'callback',
      'actionType',
      'ActivityService'
    ];
    var ActivityController = function($scope, $modalInstance, callback, actionType, ActivityService) {
      // functions
      $scope.addDrugClicked = addDrugClicked;
      $scope.treatmentAdded = treatmentAdded;
      $scope.addItem = addItem;
      $scope.editItem = editItem;
      $scope.cancel = cancel;
      $scope.typeChanged = typeChanged;

      //init
      $scope.isEditing = false;
      $scope.actionType = actionType;
      $scope.activityTypeOptions = _.values(ActivityService.ACTIVITY_TYPE_OPTIONS);
      $scope.treatmentDirective = {
        isVisible: false
      };

      $scope.$watch(function() {
        return angular.element('.scrollable-wrapper').parent().parent().height();
      }, function() {
        showScrolbarIfNessesary();
      });

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

      function addDrugClicked() {
        $scope.treatmentDirective.isVisible = true;
      }

      function treatmentAdded(treatment) {
        $scope.itemScratch.treatments.push(treatment);
        $scope.treatmentDirective.isVisible = false;
        $scope.notEnoughtTreatments = $scope.itemScratch.activityType.uri === 'ontology:TreatmentActivity' && !$scope.itemScratch.treatments;
      }

      function addItem() {
        $scope.isEditing = true;
        ActivityService.addItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create activity');
              cancel();
            });
      }

      function editItem() {
        $scope.isEditing = true;
        ActivityService.editItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to edit activity');
              cancel();
            });
      }

      function cancel() {
        $modalInstance.close();
      }

      function typeChanged() {
        showScrolbarIfNessesary();
        $scope.notEnoughtTreatments = $scope.itemScratch.activityType.uri === 'ontology:TreatmentActivity' && !$scope.itemScratch.treatments;
      }

      // private
      function showScrolbarIfNessesary() {
        var offsetString = angular.element('.reveal-modal').css('top');
        var offset = parseInt(offsetString, 10); // remove px part
        var viewPortHeight = angular.element('html').height();
        var scrollableWrapperElement = angular.element('.scrollable-wrapper');
        var wrapperHeight = scrollableWrapperElement.parent().parent().height();
        var maxWrapperHeight = viewPortHeight - offset;

        if (wrapperHeight > maxWrapperHeight) {
          scrollableWrapperElement.css('max-height', viewPortHeight - offset);
        } else {
          scrollableWrapperElement.css('max-height', viewPortHeight + offset);
        }
      }

    };
    return dependencies.concat(ActivityController);
  });