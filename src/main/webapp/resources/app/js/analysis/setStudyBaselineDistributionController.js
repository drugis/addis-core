'use strict';
define(['lodash', 'angular'], function(_) {
  var dependencies = [
    '$scope',
    'outcome',
    'measurementType',
    'referenceAlternativeName',
    'callback'
  ];
  var SetStudyBaselineDistributionController = function(
    $scope,
    outcome,
    measurementType,
    referenceAlternativeName,
    callback
  ) {
    $scope.setBaseline = callback;

    $scope.outcome = outcome;
    $scope.baselineDistribution = {};
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
        dof: undefined
      },
      hazardRatio: {
        title: 'Hazard Ratio',
        type: 'dsurv',
        responders: undefined,
        exposure: undefined,
        summaryMeasure: 'none'
      }
    };
    $scope.propertyName = properties[measurementType].title;
    $scope.baseline = properties[measurementType];

  };
  return dependencies.concat(SetStudyBaselineDistributionController);
});
