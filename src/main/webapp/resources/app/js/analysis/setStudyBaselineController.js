'use strict';
define(['lodash', 'angular'], function(_) {
  var dependencies = [
    '$scope',
    '$modalInstance',
    'outcome',
    'measurementType',
    'referenceAlternativeName',
    'callback'
  ];
  var SetStudyBaselineController = function(
    $scope,
    $modalInstance,
    outcome,
    measurementType,
    referenceAlternativeName,
    callback
  ) {
    $scope.setBaseline = setBaseline;
    $scope.cancel = $modalInstance.close;

    $scope.outcome = outcome;
    $scope.alternativeName = referenceAlternativeName;

    $scope.summaryMeasureOptions = [{
      label: 'none',
      id: 'none'
    }, {
      label: 'median survival',
      id: 'median'
    }, {
      label: 'mean survival',
      id: 'mean'
    }, {
      label: 'survival at time',
      id: 'survivalAtTime'
    }];

    var properties = {
      oddsRatio: {
        title: 'Odds Ratio',
        type: 'dbeta-logit',
        alpha: undefined,
        beta: undefined,
        property: 'log odds ratio',
        distribution: 'Beta'
      },
      meanDifference: {
        title: 'Mean Difference',
        type: 'dnorm',
        mean: undefined,
        'std.err': undefined,
        dof: undefined,
        property: 'mean difference',
        distribution: 'Normal',
        scale: 'mean difference'
      },
      hazardRatio: {
        title: 'Hazard Ratio',
        type: 'dsurv',
        alpha: undefined,
        beta: undefined,
        summaryMeasure: 'none',
        property: 'log hazard ratio',
        distribution: 'Gamma', 
        scale: 'hazard ratio'
      }
    };
    $scope.propertyName = properties[measurementType].title;
    $scope.baseline = _.merge({}, properties[measurementType], {
      name: referenceAlternativeName
    });

    function setBaseline() {
      callback($scope.baseline);
      $modalInstance.close();
    }
  };
  return dependencies.concat(SetStudyBaselineController);
});
