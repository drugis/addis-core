'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state', '$modal',
    'AnalysisResource', 'InterventionResource', 'OutcomeResource',
    'MetaBenefitRiskService', 'ModelResource', 'ProblemResource',
    'ScalesService', 'ScenarioResource', 'DEFAULT_VIEW', 'ProjectResource', 'UserService',
    'gemtcRootPath'
  ];
  var MetBenefitRiskStep2Controller = function($scope, $q, $stateParams, $state, $modal,
    AnalysisResource, InterventionResource, OutcomeResource, MetaBenefitRiskService,
    ModelResource, ProblemResource, ScalesService, ScenarioResource, DEFAULT_VIEW, ProjectResource, UserService,
    gemtcRootPath) {

    $scope.goToStep1 = goToStep1;
    $scope.openDistributionModal = openDistributionModal;

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.hasMissingBaseLine = hasMissingBaseLine;
    $scope.finalizeAndGoToDefaultScenario = finalizeAndGoToDefaultScenario;
    $scope.goToDefaultScenario = goToDefaultScenario;
    $scope.project = ProjectResource.get($stateParams);
    $scope.userId = $stateParams.userUid;

    $scope.editMode = {
      allowEditing: false
    };
    $scope.project.$promise.then(function() {
      if (UserService.isLoginUserId($scope.project.owner.id) && !$scope.analysis.archived) {
        $scope.editMode.allowEditing = true;
      }
    });
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

        analysis = addModelBaseline(analysis, models);
        analysis.$save().then(function() {
          $scope.outcomesWithAnalyses = buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses, models);
          resetScales();
        });
      });

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = _.find(analysis.interventionInclusions, function(includedIntervention) {
          return includedIntervention.interventionId === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });

      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = _.find(analysis.mbrOutcomeInclusions, function(mbrOutcomeInclusion) {
          return mbrOutcomeInclusion.outcomeId === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
    });

    function addModelBaseline(analysis, models) {
      _.forEach(analysis.mbrOutcomeInclusions, function(mbrOutcomeInclusion) {
        if (!mbrOutcomeInclusion.baseline) {
          var baselineModel = _.find(models, function(model) {
            return model.id === mbrOutcomeInclusion.modelId;
          });
          if (baselineModel && baselineModel.baseline) {
            mbrOutcomeInclusion.baseline = baselineModel.baseline.baseline;
          }
        }
      });
      return analysis;
    }





    function hasMissingBaseLine() {
      return _.find($scope.outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return !outcomeWithAnalysis.baselineDistribution;
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
        .map(function(outcomeWithAnalysis) {
          outcomeWithAnalysis.networkMetaAnalyses = outcomeWithAnalysis.networkMetaAnalyses.sort(MetaBenefitRiskService.compareAnalysesByModels);
          return outcomeWithAnalysis;
        })
        .filter(function(outcomeWithAnalysis) {
          return outcomeWithAnalysis.outcome.isIncluded;
        })
        .map(function(outcomeWithAnalysis) {
          outcomeWithAnalysis.baselineDistribution = _.find($scope.analysis.mbrOutcomeInclusions, function(inclusion) {
            return inclusion.outcomeId === outcomeWithAnalysis.outcome.id;
          }).baseline;
          return outcomeWithAnalysis;
        });
    }


    function goToStep1() {
      $state.go('MetaBenefitRiskCreationStep-1', $stateParams);
    }

    function openDistributionModal(outcomeWithAnalysis) {
      $modal.open({
        templateUrl: gemtcRootPath + 'js/models/setBaselineDistribution.html',
        controller: 'SetBaselineDistributionController',
        windowClass: 'small',
        resolve: {
          outcomeWithAnalysis: function() {
            return outcomeWithAnalysis;
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
                if (mbrOutcomeInclusion.outcomeId === outcomeWithAnalysis.outcome.id) {
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
        if (problem.performanceTable.length > 0) {
          ScalesService.getObservedScales($scope, problem).then(function(result) {
            var includedAlternatives = _.filter($scope.alternatives, function(alternative) {
              return alternative.isIncluded;
            });
            $scope.outcomesWithAnalyses = MetaBenefitRiskService.addScales($scope.outcomesWithAnalyses,
              includedAlternatives, result);
          }, function() {
            console.log('ScalesService.getObservedScales error');
          });
        }
      });
      $scope.isMissingBaseline = _.find($scope.outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return !outcomeWithAnalysis.baselineDistribution;
      });
    }

  };
  return dependencies.concat(MetBenefitRiskStep2Controller);
});
