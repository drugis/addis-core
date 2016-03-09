'use strict';
define([], function() {
  var dependencies = ['$scope', '$modalInstance', 'AnalysisService',
    'outcomeWithAnalysis', 'includedAlternatives', 'setBaselineDistribution'
  ];
  var SetBaselineDistributionController = function($scope,
    $modalInstance, AnalysisService, outcomeWithAnalysis, includedAlternatives,
    setBaselineDistribution) {

    $scope.outcomeWithAnalysis = outcomeWithAnalysis;
    $scope.includedAlternatives = includedAlternatives;

    $scope.baselineDistribution = {
      selectedAlternative: includedAlternatives[0]
    };
    $scope.baselineDistribution.scale = AnalysisService.LIKELIHOOD_LINK_SETTINGS.find(function(setting) {
      return setting.likelihood === outcomeWithAnalysis.selectedModel.likelihood &&
        setting.link === outcomeWithAnalysis.selectedModel.link;
    }).absoluteScale;

    $scope.setBaselineDistribution = function(baselineDistribution) {
      baselineDistribution.name = baselineDistribution.selectedAlternative.name;
      baselineDistribution.type = 'dnorm';
      delete baselineDistribution.selectedAlternative;
      setBaselineDistribution(baselineDistribution);
      $modalInstance.close();
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(SetBaselineDistributionController);
});
