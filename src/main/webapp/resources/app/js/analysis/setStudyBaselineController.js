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
        beta: undefined
      },
      meanDifference: {
        title: 'Mean Difference',
        type: 'dnorm',
        mean: undefined,
        'std.err': undefined,
        dof: undefined,
        scale: 'Normal'
      },
      hazardRatio: {
        title: 'Hazard Ratio',
        type: 'dsurv',
        responders: undefined,
        exposure: undefined,
        summaryMeasure: 'none',
        scale: 'Gamma'
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
