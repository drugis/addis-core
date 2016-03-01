'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'AnalysisResource', 'InterventionResource', 'OutcomeResource', 'MetaBenefitRiskService'];
  var MetBenefitRiskStep1Controller = function($scope, $q, $stateParams, AnalysisResource, InterventionResource, OutcomeResource, MetaBenefitRiskService) {
    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.updateAlternatives = updateAlternatives;
    $scope.updateMbrOutcomeInclusions = updateMbrOutcomeInclusions;
    $scope.updateAnalysesInclusions = updateAnalysesInclusions;

    $q.all([$scope.analysis.$promise, $scope.outcomes.$promise]).then(function(result) {
      var analysis = result[0];
      var outcomes = result[1];
      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });
      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        $scope.outcomesWithAnalyses = outcomes.map(function(outcome) {
          return MetaBenefitRiskService.buildOutcomesWithAnalyses(outcome, analysis, networkMetaAnalyses);
        });
      });
    });

    $q.all([$scope.analysis.$promise, $scope.alternatives.$promise]).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = analysis.includedAlternatives.find(function(includedAlternative) {
          return includedAlternative.id === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });
    });

    $q.all([$scope.analysis.$promise, $scope.outcomes.$promise]).then(function(result) {
      var analysis = result[0];
      var outcomes = result[1];
      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = analysis.mbrOutcomeInclusions.find(function(mbrOutcomeInclusion) {
          return mbrOutcomeInclusion.outcomeId === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
    });

    function updateAlternatives() {
      $scope.analysis.includedAlternatives = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
      });
      $scope.analysis.$save();
    }

    function initAnalysisRadios(outcomeWithAnalyses) {
      if (outcomeWithAnalyses.outcome.isIncluded) {
        outcomeWithAnalyses.selectedAnalysisId = outcomeWithAnalyses.networkMetaAnalyses[0].id;
      } else {
        outcomeWithAnalyses.selectedAnalysisId = undefined;
      }
    }

    function updateAnalysesInclusions() {
      buildInclusions();
    }

    function updateMbrOutcomeInclusions(changedOutcome) {
      initAnalysisRadios(changedOutcome);
      buildInclusions();
    }

    function buildInclusions() {
      $scope.analysis.mbrOutcomeInclusions = $scope.outcomesWithAnalyses.filter(function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.outcome.isIncluded;
      }).map(function(outcomeWithAnalyses) {
        return {
          metaBenefitRiskAnalysisId: $scope.analysis.id,
          outcomeId: outcomeWithAnalyses.outcome.id,
          networkMetaAnalysisId: outcomeWithAnalyses.selectedAnalysisId
        };
      });
      $scope.analysis.$save();
    }

  };
  return dependencies.concat(MetBenefitRiskStep1Controller);
});
