'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$modalInstance',
    'OutcomeService',
    'itemService',
    'MeasurementMomentService',
    'ResultsService',
    'callback',
    'item',
    'itemType',
    'TIME_SCALE_OPTIONS',
    'VARIABLE_TYPE_DETAILS'
  ];
  var EditVariableController = function(
    $scope,
    $modalInstance,
    OutcomeService,
    itemService,
    MeasurementMomentService,
    ResultsService,
    callback,
    item,
    itemType,
    TIME_SCALE_OPTIONS,
    VARIABLE_TYPE_DETAILS
  ) {
    // functions
    $scope.measurementMomentEquals = measurementMomentEquals;
    $scope.deleteCategory = deleteCategory;
    $scope.addCategory = addCategory;
    $scope.addCategoryEnterKey = addCategoryEnterKey;
    $scope.editVariable = editVariable;
    $scope.resetResultProperties = resetResultProperties;
    $scope.cancel = cancel;
    $scope.armOrContrastChanged = armOrContrastChanged;

    // init
    $scope.isEditing = false;
    $scope.variable = item;
    $scope.itemType = itemType;
    setArmOrContrast();
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = _.values(VARIABLE_TYPE_DETAILS[$scope.variable.armOrContrast]);
    $scope.timeScaleOptions = TIME_SCALE_OPTIONS;

    $scope.$watch('variable.selectedResultProperties', checkTimeScaleInput);

    $scope.variable.selectedResultProperties = getSelectedResultProperties();

    function setArmOrContrast() {
      if (!$scope.variable.armOrContrast) {
        $scope.variable.armOrContrast = 'ontology:arm_level_data';
      }
    }

    function getSelectedResultProperties() {
      return _.filter($scope.resultProperties, function(resultProperty) {
        return _.includes($scope.variable.resultProperties, resultProperty.uri);
      });
    }

    function checkTimeScaleInput() {
      $scope.showTimeScaleInput = _.find($scope.variable.selectedResultProperties, ['uri', 'http://trials.drugis.org/ontology#exposure']);
      if (!$scope.showTimeScaleInput) {
        delete $scope.variable.timeScale;
      } else {
        if (!$scope.variable.timeScale) {
          $scope.variable.timeScale = 'P1W';
        }
      }
    }

    function measurementMomentEquals(moment1, moment2) {
      return moment1.uri === moment2.uri;
    }

    function resetResultProperties() {
      $scope.variable.armOrContrast = 'ontology:arm_level_data';
      armOrContrastChanged();
      if ($scope.variable.measurementType === 'ontology:categorical') {
        $scope.variable.categoryList = [];
        $scope.newCategory = {};
      } else {
        delete $scope.variable.categoryList;
        delete $scope.newCategory;
      }
    }

    function editVariable() {
      $scope.isEditing = false;
      $scope.resultProperties = _.map($scope.variable.selectedResultProperties, 'uri');
      delete $scope.variable.selectedResultProperties;
      itemService.editItem($scope.variable).then(succesCallback, errorCallback);
    }

    function succesCallback() {
      callback();
      $modalInstance.close();
    }

    function errorCallback() {
      $modalInstance.close('cancel');
    }

    function deleteCategory(toDelete) {
      $scope.variable.categoryList = _.reject($scope.variable.categoryList, function(category) {
        return toDelete['@id'] === category['@id'];
      });
    }

    function isDuplicateCategory(newCategory) {
      return _.includes(
        _.map($scope.variable.categoryList, 'label'),
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
        $scope.variable.categoryList.push(newCategoryObj);
        newCategory.categoryLabel = '';
      }
    }

    function cancel() {
      $modalInstance.close('cancel');
    }

    function armOrContrastChanged() {
      $scope.variable.resultProperties = VARIABLE_TYPE_DETAILS[$scope.variable.armOrContrast];
      $scope.variable.selectedResultProperties = ResultsService.getDefaultResultProperties($scope.variable.measurementType, $scope.variable.armOrContrast);
    }
  };
  return dependencies.concat(EditVariableController);
});
