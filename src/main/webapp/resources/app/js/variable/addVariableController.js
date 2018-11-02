'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$injector',
    '$modalInstance',
    'MeasurementMomentService',
    'ResultsService',
    'arms',
    'callback',
    'settings',
    'TIME_SCALE_OPTIONS',
    'VARIABLE_TYPE_DETAILS',
    'ARM_LEVEL_TYPE',
    'CONTRAST_TYPE'
  ];
  var addVariableController = function(
    $scope,
    $injector,
    $modalInstance,
    MeasurementMomentService,
    ResultsService,
    arms,
    callback,
    settings,
    TIME_SCALE_OPTIONS,
    VARIABLE_TYPE_DETAILS,
    ARM_LEVEL_TYPE,
    CONTRAST_TYPE
  ) {
    // functions
    $scope.addVariable = addVariable;
    $scope.resetResultProperties = resetResultProperties;
    $scope.addCategory = addCategory;
    $scope.cannotAddCategory = cannotAddCategory;
    $scope.isDuplicateCategory = isDuplicateCategory;
    $scope.deleteCategory = deleteCategory;
    $scope.addCategoryEnterKey = addCategoryEnterKey;
    $scope.measurementMomentEquals = measurementMomentEquals;
    $scope.cancel = cancel;
    $scope.armOrContrastChanged = armOrContrastChanged;

    // init
    var Service = $injector.get(settings.service);
    $scope.settings = settings;
    $scope.variable = {
      measuredAtMoments: [],
      resultProperties: [],
      measurementType: 'ontology:dichotomous'
    };
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.timeScaleOptions = TIME_SCALE_OPTIONS;
    $scope.arms = arms;

    resetResultProperties();

    $scope.$watch('item.selectedResultProperties', checkTimeScaleInput);

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
      $scope.variable.armOrContrast = ARM_LEVEL_TYPE;
      armOrContrastChanged();
      if ($scope.variable.measurementType === 'ontology:categorical') {
        $scope.variable.categoryList = [];
        $scope.newCategory = {};
      } else {
        delete $scope.variable.categoryList;
        delete $scope.newCategory;
      }
    }

    function addVariable() {
      $scope.variable.resultProperties = _.map($scope.variable.selectedResultProperties, 'uri');
      Service.addItem($scope.variable).then(succesCallback, errorCallback);
    }

    function succesCallback() {
      callback();
      $modalInstance.close();
    }

    function errorCallback() {
      console.error('failed to create ' + settings.itemName);
      $modalInstance.close();
    }

    function addCategoryEnterKey($event, newCategory) {
      if ($event.keyCode === 13 && !cannotAddCategory(newCategory)) {
        addCategory(newCategory);
      }
    }

    function addCategory(newCategory) {
      if (!cannotAddCategory(newCategory)) {
        $scope.variable.categoryList.push(_.trim(newCategory.categoryLabel));
        newCategory.categoryLabel = '';
      }
    }

    function isDuplicateCategory(newCategory) {
      return _.includes($scope.variable.categoryList, _.trim(newCategory.categoryLabel));
    }

    function cannotAddCategory(newCategory) {
      return !_.trim(newCategory.categoryLabel) || isDuplicateCategory(newCategory);
    }

    function deleteCategory(category) {
      $scope.variable.categoryList.splice($scope.variable.categoryList.indexOf(category), 1);
    }

    function cancel() {
      $modalInstance.close('cancel');
    }

    function armOrContrastChanged() {
      $scope.variable.resultProperties = VARIABLE_TYPE_DETAILS[$scope.variable.armOrContrast];
      $scope.variable.selectedResultProperties = ResultsService.getDefaultResultProperties($scope.variable.measurementType, $scope.variable.armOrContrast);
      if($scope.variable.armOrContrast === CONTRAST_TYPE){
        $scope.variable.referenceArm = $scope.arms[0];
      }
    }
  };
  return dependencies.concat(addVariableController);
});
