'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$injector', '$modalInstance', 'MeasurementMomentService', 'ResultsService', 'callback', 'settings'];
  var addVariableController = function($scope, $injector, $modalInstance, MeasurementMomentService, ResultsService, callback, settings) {
    // functions
    $scope.addItem = addItem;
    $scope.resetResultProperties = resetResultProperties;
    $scope.addCategory = addCategory;
    $scope.cannotAddCategory = cannotAddCategory;
    $scope.isDuplicateCategory = isDuplicateCategory;
    $scope.deleteCategory = deleteCategory;
    $scope.addCategoryEnterKey = addCategoryEnterKey;
    $scope.measurementMomentEquals = measurementMomentEquals;
    $scope.cancel = cancel;

    // init
    var service = $injector.get(settings.service);
    $scope.settings = settings;
    $scope.item = {
      measuredAtMoments: [],
      resultProperties: [],
      measurementType: 'ontology:dichotomous'
    };
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = ResultsService.VARIABLE_TYPE_DETAILS;

    resetResultProperties();

    function measurementMomentEquals(moment1, moment2) {
      return moment1.uri === moment2.uri;
    }

    function resetResultProperties() {
      $scope.item.selectedResultProperties = ResultsService.getDefaultResultProperties($scope.item.measurementType);
      if ($scope.item.measurementType === 'ontology:categorical') {
        $scope.item.categoryList = [];
        $scope.newCategory = {};
      } else {
        delete $scope.item.categoryList;
        delete $scope.newCategory;
      }
    }

    function addItem() {
      $scope.item.resultProperties = _.map($scope.item.selectedResultProperties, 'uri');
      service.addItem($scope.item)
        .then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            console.error('failed to create ' + settings.itemName);
            $modalInstance.dismiss('cancel');
          });
    }

    function addCategoryEnterKey($event, newCategory) {
      if ($event.keyCode === 13 && !cannotAddCategory(newCategory)) {
        addCategory(newCategory);
      }
    }

    function addCategory(newCategory) {
      if (!cannotAddCategory(newCategory)) {
        $scope.item.categoryList.push(_.trim(newCategory.categoryLabel));
        newCategory.categoryLabel = '';
      }
    }

    function isDuplicateCategory(newCategory) {
      return _.includes($scope.item.categoryList, _.trim(newCategory.categoryLabel));
    }

    function cannotAddCategory(newCategory) {
      return !_.trim(newCategory.categoryLabel) || isDuplicateCategory(newCategory);
    }

    function deleteCategory(category) {
      $scope.item.categoryList.splice(
        $scope.item.categoryList.indexOf(category), 1);
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  };
  return dependencies.concat(addVariableController);
});