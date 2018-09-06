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

      $scope.$watch(scrollableWrapperHeight, showScrollbarIfNecessary);

      function scrollableWrapperHeight() {
        var scrollableWrappers = document.getElementsByClassName('scrollable-wrapper');
        if(!scrollableWrappers.length) { return ; }
        return angular.element(scrollableWrappers[0]).parent().parent()[0].clientHeight;
      }

      function showScrollbarIfNecessary(oldValue, newValue) {
        if (oldValue === newValue) { return ; }
        var revealModal = document.getElementsByClassName('reveal-modal');
        if (!revealModal.length) { return ; }
        var offsetString = angular.element(revealModal).css('top');
        var offset = parseInt(offsetString, 10); // remove px part
        var viewPortHeight = angular.element('html')[0].clientHeight;
        var wrapperHeight = scrollableWrapperHeight();
        var maxWrapperHeight = viewPortHeight - offset;

        if (wrapperHeight > maxWrapperHeight) {
          scrollableWrapperElement.css('max-height', viewPortHeight - offset);
        } else {
          scrollableWrapperElement.css('max-height', viewPortHeight + offset);
        }
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

      function addDrugClicked() {
        $scope.treatmentDirective.isVisible = true;
      }

      function treatmentAdded(treatment) {
        $scope.itemScratch.treatments.push(treatment);
        $scope.treatmentDirective.isVisible = false;
        $scope.notEnoughTreatments = $scope.itemScratch.activityType.uri === 'ontology:TreatmentActivity' && !$scope.itemScratch.treatments;
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
        showScrollbarIfNecessary();
        $scope.notEnoughTreatments = $scope.itemScratch.activityType.uri === 'ontology:TreatmentActivity' && !$scope.itemScratch.treatments;
      }

      // private


    };
    return dependencies.concat(ActivityController);
  });