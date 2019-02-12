'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$modalInstance',
    '$stateParams',
    'ArmService',
    'OutcomeService',
    'itemService',
    'MeasurementMomentService',
    'ResultPropertiesService',
    'callback',
    'item',
    'itemType',
    'TIME_SCALE_OPTIONS',
    'RESULT_PROPERTY_TYPE_DETAILS',
    'ARM_LEVEL_TYPE',
    'CONTRAST_TYPE'
  ];
  var EditVariableController = function(
    $scope,
    $modalInstance,
    $stateParams,
    ArmService,
    OutcomeService,
    itemService,
    MeasurementMomentService,
    ResultPropertiesService,
    callback,
    item,
    itemType,
    TIME_SCALE_OPTIONS,
    RESULT_PROPERTY_TYPE_DETAILS,
    ARM_LEVEL_TYPE,
    CONTRAST_TYPE
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
    $scope.variable = initVariable(item);
    $scope.itemType = itemType;
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    
    $scope.timeScaleOptions = TIME_SCALE_OPTIONS;
    $scope.$watch('variable.selectedResultProperties', checkTimeScaleInput, true);
    
    
    getArms();
    
    function initVariable(variable) {
      variable.armOrContrast = getArmOrContrast(variable);
      $scope.resultProperties = _.values(RESULT_PROPERTY_TYPE_DETAILS[$scope.variable.armOrContrast]);
      $scope.variable.selectedResultProperties = getSelectedResultProperties();
      if (variable.armOrContrast === CONTRAST_TYPE) {
        variable.contrastOptions = ResultPropertiesService.getContrastOptions(variable.measurementType);
        variable.contrastOption = _.find(variable.contrastOptions, function(option) {
          return _.includes(variable.resultProperties, option.uri);
        });
        variable.isLog = !!variable.isLog;
      }
      return variable;
    }

    function getArmOrContrast(variable) {
      return variable.armOrContrast ? variable.armOrContrast : ARM_LEVEL_TYPE;
    }

    function getSelectedResultProperties() {
      return _.filter($scope.resultProperties, function(resultProperty) {
        return _.includes($scope.variable.resultProperties, resultProperty.uri);
      });
    }

    function checkTimeScaleInput(newVal, oldVal) {
      if (_.isEqual(newVal, oldVal)) { return; }
      $scope.variable = ResultPropertiesService.setTimeScaleInput($scope.variable);
      $scope.showTimeScaleInput = !!$scope.variable.timeScale;
    }

    function measurementMomentEquals(moment1, moment2) {
      return moment1.uri === moment2.uri;
    }

    function resetResultProperties() {
      $scope.variable = ResultPropertiesService.resetResultProperties($scope.variable, $scope.arms);
      if ($scope.variable.measurementType === 'ontology:categorical') {
        $scope.newCategory = {};
      } else {
        delete $scope.newCategory;
      }
    }

    function editVariable() {
      $scope.isEditing = false;
      if ($scope.variable.armOrContrast === CONTRAST_TYPE) {
        $scope.variable.selectedResultProperties = [$scope.variable.contrastOption].concat($scope.variable.selectedResultProperties);
      }
      $scope.variable.resultProperties = _.map($scope.variable.selectedResultProperties, 'uri');
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
        _.trim(newCategory.categoryLabel)
      );
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
      $scope.variable = ResultPropertiesService.armOrContrastChanged($scope.variable, $scope.arms);
    }

    function getArms() {
      return ArmService.queryItems($stateParams.studyUUID).then(function(result) {
        $scope.arms = result;
      });
    }
  };
  return dependencies.concat(EditVariableController);
});
