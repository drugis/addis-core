'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$stateParams',
    '$scope',
    '$injector',
    '$modalInstance',
    'MeasurementMomentService',
    'ResultPropertiesService',
    'ArmService',
    'callback',
    'settings',
    'TIME_SCALE_OPTIONS',
    'DICHOTOMOUS_TYPE'
  ];
  var addVariableController = function(
    $stateParams,
    $scope,
    $injector,
    $modalInstance,
    MeasurementMomentService,
    ResultPropertiesService,
    ArmService,
    callback,
    settings,
    TIME_SCALE_OPTIONS,
    DICHOTOMOUS_TYPE
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
      measurementType: DICHOTOMOUS_TYPE
    };
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.timeScaleOptions = TIME_SCALE_OPTIONS;
    getArms();

    resetResultProperties();

    $scope.$watch('item.selectedResultProperties', checkTimeScaleInput);

    function measurementMomentEquals(moment1, moment2) {
      return moment1.uri === moment2.uri;
    }

    function checkTimeScaleInput(newVal, oldVal) {
      if (_.isEqual(newVal, oldVal)) { return; }
      $scope.variable = ResultPropertiesService.setTimeScaleInput($scope.variable);
      $scope.showTimeScaleInput = !!$scope.variable.timeScale;
    }

    function resetResultProperties() {
      $scope.variable = ResultPropertiesService.resetResultProperties($scope.variable, $scope.arms);
      if ($scope.variable.measurementType === 'ontology:categorical') {
        $scope.newCategory = {};
      } else {
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
      $scope.variable = ResultPropertiesService.armOrContrastChanged($scope.variable, $scope.arms);
    }

    function getArms() {
      return ArmService.queryItems($stateParams.studyUUID).then(function(result) {
        $scope.arms = result;
      });
    }
  };
  return dependencies.concat(addVariableController);
});
