'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$state', '$modalInstance',
    'OutcomeService', 'itemService', 'MeasurementMomentService',
    'ResultsService', 'callback', 'item', 'itemType'
  ];
  var EditItemController = function($scope, $state, $modalInstance,
    OutcomeService, itemService, MeasurementMomentService,
    ResultsService, callback, item, itemType) {
    // functions
    $scope.measurementMomentEquals = measurementMomentEquals;
    $scope.deleteCategory = deleteCategory;
    $scope.addCategory = addCategory;
    $scope.addCategoryEnterKey = addCategoryEnterKey;
    $scope.editItem = editItem;
    $scope.resetResultProperties = resetResultProperties;
    $scope.cancel = cancel;

    // init
    $scope.isEditing = false;
    $scope.item = item;
    $scope.itemType = itemType;
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = _.map(ResultsService.VARIABLE_TYPE_DETAILS, _.identity);

    item.selectedResultProperties = _.filter($scope.resultProperties, function(resultProperty) {
      return _.includes(item.resultProperties, resultProperty.uri);
    });

    function measurementMomentEquals(moment1, moment2) {
      return moment1.uri === moment2.uri;
    }

    function resetResultProperties() {
      item.selectedResultProperties = ResultsService.getDefaultResultProperties($scope.item.measurementType);
      if ($scope.item.measurementType === 'ontology:categorical') {
        $scope.item.categoryList = [];
        $scope.newCategory = {};
      } else {
        delete $scope.item.categoryList;
        delete $scope.newCategory;
      }
    }

    function editItem() {
      $scope.isEditing = false;
      item.resultProperties = _.map(item.selectedResultProperties, 'uri');
      delete item.selectedResultProperties;
      itemService.editItem($scope.item).then(function() {
          callback();
          $modalInstance.close();
        },
        function() {
          $modalInstance.dismiss('cancel');
        });
    }

    function deleteCategory(toDelete) {
      $scope.item.categoryList = _.reject($scope.item.categoryList, function(category) {
        return toDelete['@id'] === category['@id'];
      });
    }

    function isDuplicateCategory(newCategory) {
      return _.includes(
        _.map($scope.item.categoryList, 'label'),
        _.trim(newCategory.categoryLabel));
    }

    function cannotAddCategory(newCategory) {
      return !_.trim(newCategory.categoryLabel) || isDuplicateCategory(newCategory);
    }

    function addCategoryEnterKey($event, newCategory) {
      if ($event.keyCode === 13 && !cannotAddCategory(newCategory)) {
        addCategory(newCategory);
      }
    }

    function addCategory(newCategory) {
      if (!cannotAddCategory(newCategory)) {
        var newCategoryObj = OutcomeService.makeCategoryIfNeeded(_.trim(newCategory.categoryLabel));
        $scope.item.categoryList.push(newCategoryObj);
        newCategory.categoryLabel = '';
      }
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  };
  return dependencies.concat(EditItemController);
});