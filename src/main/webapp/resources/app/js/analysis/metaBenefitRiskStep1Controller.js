'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', 'AnalysisResource', 'InterventionResource',
    'OutcomeResource', 'MetaBenefitRiskService', 'ModelResource'
  ];
  var MetBenefitRiskStep1Controller = function($scope, $q, $stateParams, AnalysisResource, InterventionResource,
    OutcomeResource, MetaBenefitRiskService, ModelResource) {
    $scope.updateAlternatives = updateAlternatives;
    $scope.updateMbrOutcomeInclusions = updateMbrOutcomeInclusions;
    $scope.updateAnalysesInclusions = updateAnalysesInclusions;
    $scope.isOutcomeDisabled = isOutcomeDisabled;

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);

    $q.all([$scope.analysis.$promise, $scope.outcomes.$promise, $scope.models.$promise]).then(function(result) {
      var analysis = result[0];
      var outcomes = result[1];
      var models = result[2];
      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });
      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses.map(_.partial(MetaBenefitRiskService.joinModelsWithAnalysis, models));
        $scope.outcomesWithAnalyses = outcomes
          .map(_.partial(MetaBenefitRiskService.buildOutcomesWithAnalyses, analysis, networkMetaAnalyses))
          .map(function(owa) {
            owa.networkMetaAnalyses = owa.networkMetaAnalyses.sort(MetaBenefitRiskService.compareAnalysesByModels);
            return owa;
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

    function isOutcomeDisabled(outcomeWithAnalyses) {
      return !outcomeWithAnalyses.networkMetaAnalyses.length ||
        !hasSelectableAnalysis(outcomeWithAnalyses);
    }

    function updateAlternatives() {
      $scope.analysis.includedAlternatives = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
      });
      $scope.analysis.$save();
    }

    function initAnalysisRadios(outcomeWithAnalyses) {
      if (hasSelectableAnalysis(outcomeWithAnalyses) && outcomeWithAnalyses.outcome.isIncluded) {
        outcomeWithAnalyses.selectedAnalysisId = outcomeWithAnalyses.networkMetaAnalyses[0].id;
      } else {
        outcomeWithAnalyses.selectedAnalysisId = undefined;
      }
    }

    function hasSelectableAnalysis(outcomeWithAnalyses) {
      var firstAnalysis = outcomeWithAnalyses.networkMetaAnalyses[0];
      return firstAnalysis && firstAnalysis.models.length;
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
