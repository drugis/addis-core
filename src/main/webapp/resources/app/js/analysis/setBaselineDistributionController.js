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
      outcomeWithAnalysis.baselineDistribution = baselineDistribution;
      setBaselineDistribution(baselineDistribution);
      $modalInstance.close();
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(SetBaselineDistributionController);
});
