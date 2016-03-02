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
    $scope.updateModelSelection = updateModelSelection;

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);

    var promises = [$scope.analysis.$promise, $scope.alternatives.$promise, $scope.outcomes.$promise, $scope.models.$promise];

    $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = result[3];
      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });

      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses = networkMetaAnalyses
          .map(_.partial(MetaBenefitRiskService.joinModelsWithAnalysis, models))
          .map(MetaBenefitRiskService.addModelsGroup);
        $scope.outcomesWithAnalyses = outcomes
          .map(_.partial(MetaBenefitRiskService.buildOutcomesWithAnalyses, analysis, networkMetaAnalyses, models))
          .map(function(owa) {
            owa.networkMetaAnalyses = owa.networkMetaAnalyses.sort(MetaBenefitRiskService.compareAnalysesByModels);
            return owa;
          });
        updateMissingAlternativesForAllOutcomes();
      });

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = analysis.includedAlternatives.find(function(includedAlternative) {
          return includedAlternative.id === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });
      setIncludedAlternatives();

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

    function setIncludedAlternatives() {
      $scope.analysis.includedAlternatives = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
      });
    }

    function updateMissingAlternativesForAllOutcomes() {
      $scope.outcomesWithAnalyses.filter(function(outcome) {
        return outcome.selectedModel;
      }).forEach(function(outcome) {
        updateMissingAlternatives(outcome);
      });
    }

    function updateAlternatives() {
      setIncludedAlternatives();
      updateMissingAlternativesForAllOutcomes();
      $scope.analysis.$save();
    }

    function changeAnalysisSelection(outcomeWithAnalyses) {
      var analysis;
      if (hasSelectableAnalysis(outcomeWithAnalyses) && outcomeWithAnalyses.outcome.isIncluded) {
        analysis = outcomeWithAnalyses.networkMetaAnalyses[0];
        outcomeWithAnalyses.selectedAnalysis = analysis;
      } else {
        outcomeWithAnalyses.selectedAnalysis = undefined;
      }
      return analysis;
    }

    function hasSelectableAnalysis(outcomeWithAnalyses) {
      var firstAnalysis = outcomeWithAnalyses.networkMetaAnalyses[0];
      return firstAnalysis && firstAnalysis.models.length;
    }

    function changeModelSelection(changedOutcome) {
      var selectedNma = changedOutcome.selectedAnalysis;
      if (selectedNma !== undefined) {
        var primaryModel = selectedNma.models.find(function(model) {
          return model.id === selectedNma.primaryModel;
        });
        if (primaryModel) {
          changedOutcome.selectedModel = primaryModel;
        } else {
          changedOutcome.selectedModel = selectedNma.models[0];
        }
      } else {
        changedOutcome.selectedModel = undefined;
      }
    }

    function updateAnalysesInclusions(changedOutcome) {
      changeModelSelection(changedOutcome);
      buildInclusions();
    }

    function updateMbrOutcomeInclusions(changedOutcome) {
      changeAnalysisSelection(changedOutcome);
      updateAnalysesInclusions(changedOutcome);
    }

    function updateModelSelection(outcome) {
      updateMissingAlternatives(outcome);
      buildInclusions();
    }

    function updateMissingAlternatives(outcome) {
      outcome.selectedModel.missingAlternatives = $scope.analysis.includedAlternatives.filter(function(alternative) {
        var modelType = outcome.selectedModel.modelType;
        if (modelType.type === 'pairwise') {
          return alternative.id !== modelType.details.from.id &&
            alternative.id !== modelType.details.to.id;
        } else {
          return outcome.selectedAnalysis.includedInterventions.find(function(includedIntervention) {
            return alternative.id !== includedIntervention.interventionId;
          });
        }
      });
      outcome.selectedModel.missingAlternativesNames = _.map(outcome.selectedModel.missingAlternatives, 'name');
    }

    function buildInclusions() {
      $scope.analysis.mbrOutcomeInclusions = $scope.outcomesWithAnalyses.filter(function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.outcome.isIncluded;
      }).map(function(outcomeWithAnalyses) {
        return {
          metaBenefitRiskAnalysisId: $scope.analysis.id,
          outcomeId: outcomeWithAnalyses.outcome.id,
          networkMetaAnalysisId: outcomeWithAnalyses.selectedAnalysis.id,
          modelId: outcomeWithAnalyses.selectedModel.id
        };
      });
      $scope.analysis.$save();
    }

  };
  return dependencies.concat(MetBenefitRiskStep1Controller);
});
