'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state', '$modal',
    'AnalysisResource', 'InterventionResource', 'OutcomeResource',
    'MetaBenefitRiskService', 'ModelResource', 'ProblemResource',
    'ScalesService', 'ScenarioResource', 'DEFAULT_VIEW'
  ];
  var MetBenefitRiskStep2Controller = function($scope, $q, $stateParams, $state, $modal,
    AnalysisResource, InterventionResource, OutcomeResource, MetaBenefitRiskService,
    ModelResource, ProblemResource, ScalesService, ScenarioResource, DEFAULT_VIEW) {

    $scope.goToStep1 = goToStep1;
    $scope.openDistributionModal = openDistributionModal;

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.hasMissingBaseLine = hasMissingBaseLine;
    $scope.finalizeAndGoToDefaultScenario = finalizeAndGoToDefaultScenario;
    $scope.goToDefaultScenario = goToDefaultScenario;

    var promises = [$scope.analysis.$promise, $scope.alternatives.$promise, $scope.outcomes.$promise, $scope.models.$promise];

    $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = result[3];
      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });

      $scope.networkMetaAnalyses = AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      });
      $scope.networkMetaAnalyses.$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses = networkMetaAnalyses
          .map(_.partial(MetaBenefitRiskService.joinModelsWithAnalysis, models))
          .map(MetaBenefitRiskService.addModelsGroup);

        $scope.outcomesWithAnalyses = buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses, models);
        resetScales();
      });

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = analysis.interventionInclusions.find(function(includedIntervention) {
          return includedIntervention.interventionId === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });

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

    function hasMissingBaseLine() {
      return _.find($scope.outcomesWithAnalyses, function(owa) {
        return !owa.baselineDistribution;
      });
    }

    function finalizeAndGoToDefaultScenario() {
      $scope.analysis.finalized = true;
      $scope.analysis.$save().then(goToDefaultScenario);
    }

    function goToDefaultScenario() {
      ScenarioResource
        .query(_.omit($stateParams, 'id'))
        .$promise
        .then(function(scenarios) {
          $state.go(DEFAULT_VIEW, {
            userUid: $scope.userId,
            projectId: $stateParams.projectId,
            analysisId: $stateParams.analysisId,
            id: scenarios[0].id
          });
        });
    }

    function buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses, models) {
      return outcomes
        .map(_.partial(MetaBenefitRiskService.buildOutcomesWithAnalyses, analysis, networkMetaAnalyses, models))
        .map(function(owa) {
          owa.networkMetaAnalyses = owa.networkMetaAnalyses.sort(MetaBenefitRiskService.compareAnalysesByModels);
          return owa;
        })
        .filter(function(owa) {
          return owa.outcome.isIncluded;
        })
        .map(function(owa) {
          owa.baselineDistribution = $scope.analysis.mbrOutcomeInclusions.find(function(inclusion) {
            return inclusion.outcomeId === owa.outcome.id;
          }).baseline;
          return owa;
        });
    }


    function goToStep1() {
      $state.go('MetaBenefitRiskCreationStep-1', $stateParams);
    }

    function openDistributionModal(owa) {
      $modal.open({
        templateUrl: './app/js/analysis/setBaselineDistribution.html',
        controller: 'SetBaselineDistributionController',
        windowClass: 'small',
        resolve: {
          outcomeWithAnalysis: function() {
            return owa;
          },
          alternatives: function() {
            return $scope.alternatives;
          },
          interventionInclusions: function() {
            return $scope.analysis.interventionInclusions;
          },
          setBaselineDistribution: function() {
            return function(baseline) {
              $scope.analysis.mbrOutcomeInclusions.map(function(mbrOutcomeInclusion) {
                if (mbrOutcomeInclusion.outcomeId === owa.outcome.id) {
                  return _.extend(mbrOutcomeInclusion, {
                    baseline: baseline
                  });
                } else {
                  return mbrOutcomeInclusion;
                }
              });
              $scope.analysis.$save().then(function() {
                $scope.outcomesWithAnalyses = buildOutcomesWithAnalyses($scope.analysis, $scope.outcomes, $scope.networkMetaAnalyses, $scope.models);
                resetScales();
              });
            };
          }
        }
      });
    }

    function resetScales() {
      ProblemResource.get($stateParams).$promise.then(function(problem) {
        ScalesService.getObservedScales($scope, problem).then(function(result) {
          var includedAlternatives = _.filter($scope.alternatives, function(alternative){
            return alternative.isIncluded;
          });
          $scope.outcomesWithAnalyses = MetaBenefitRiskService.addScales($scope.outcomesWithAnalyses,
            includedAlternatives, result);
        }, function() {
          console.log('ScalesService.getObservedScales error');
        });
      });
    }

  };
  return dependencies.concat(MetBenefitRiskStep2Controller);
});
