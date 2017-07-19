'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state', '$modal',
    'AnalysisResource',
    'InterventionResource',
    'OutcomeResource',
    'BenefitRiskService',
    'ModelResource',
    'ProblemResource',
    'ScalesService',
    'ScenarioResource',
    'DEFAULT_VIEW',
    'ProjectResource',
    'UserService',
    'gemtcRootPath',
    'WorkspaceService',
    'SubProblemResource'
  ];
  var BenefitRiskStep2Controller = function($scope, $q, $stateParams, $state, $modal,
    AnalysisResource,
    InterventionResource,
    OutcomeResource,
    BenefitRiskService,
    ModelResource,
    ProblemResource,
    ScalesService,
    ScenarioResource,
    DEFAULT_VIEW,
    ProjectResource,
    UserService,
    gemtcRootPath,
    WorkspaceService,
    SubProblemResource) {

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
      allowEditing: false,
      loaded: false
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
      var models = _.reject(result[3], 'archived');

      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
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

      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        var filteredNetworkMetaAnalyses = _.chain(networkMetaAnalyses)
          .reject(function(analysis) {
            return analysis.archived;
          })
          .map(_.partial(BenefitRiskService.joinModelsWithAnalysis, models))
          .map(BenefitRiskService.addModelsGroup)
          .value();
        $scope.networkMetaAnalyses = filteredNetworkMetaAnalyses;

        analysis = addModelBaseline(analysis, models);
        var saveCommand = analysisToSaveCommand($scope.analysis);
        AnalysisResource.save(saveCommand, function() {
          $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(analysis, outcomes, filteredNetworkMetaAnalyses);
          resetScales();
        });
      });

      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = _.find(analysis.benefitRiskNMAOutcomeInclusions, function(benefitRiskNMAOutcomeInclusion) {
          return benefitRiskNMAOutcomeInclusion.outcomeId === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
      
      $scope.editMode.loaded = true;
    });

    function addModelBaseline(analysis, models) {
      _.forEach(analysis.benefitRiskNMAOutcomeInclusions, function(benefitRiskNMAOutcomeInclusion) {
        if (!benefitRiskNMAOutcomeInclusion.baseline) {
          // there is no baseline set yet, check if you can use the modelBaseline
          var baselineModel = _.find(models, function(model) {
            return model.id === benefitRiskNMAOutcomeInclusion.modelId;
          });
          if (baselineModel && baselineModel.baseline) {
            // there is a model with a baseline, yay
            if (_.find(analysis.interventionInclusions, function(interventionInclusion) {
                //there is an intervention with the right name!
                return _.find($scope.alternatives, function(alternative) {
                  return interventionInclusion.interventionId === alternative.id;
                }).name.localeCompare(baselineModel.baseline.baseline.name) === 0;
              })) {
              benefitRiskNMAOutcomeInclusion.baseline = baselineModel.baseline.baseline;
            }
          }
        }
      });
      return analysis;
    }

    function hasMissingBaseLine() {
      return _.find($scope.outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return outcomeWithAnalysis.dataType === 'network' && !outcomeWithAnalysis.baselineDistribution;
      });
    }

    function analysisToSaveCommand(analysis, problem) {
      var analysisToSave = angular.copy(analysis);
      return {
        id: analysis.id,
        projectId: analysis.projectId,
        analysis: analysisToSave,
        scenarioState: JSON.stringify(problem, null, 2)
      };
    }

    function finalizeAndGoToDefaultScenario() {
      $scope.analysis.finalized = true;
      ProblemResource.get($stateParams).$promise.then(function(problem) {
        var saveCommand = analysisToSaveCommand($scope.analysis, {
          problem: WorkspaceService.reduceProblem(problem)
        });
        AnalysisResource.save(saveCommand, function() {
          goToDefaultScenario();
        });
      });
    }

    function goToDefaultScenario() {
      var params = $stateParams;
      SubProblemResource.query(params).$promise.then(function(subProblems) {
        var subProblem = subProblems[0];
        params = _.extend({}, params, {
          problemId: subProblem.id
        });
        ScenarioResource.query(params).$promise.then(function(scenarios) {
          $state.go(DEFAULT_VIEW, {
            userUid: $scope.userId,
            projectId: params.projectId,
            analysisId: params.analysisId,
            problemId: subProblem.id,
            id: scenarios[0].id
          });
        });
      });
    }

    function goToStep1() {
      $state.go('BenefitRiskCreationStep-1', $stateParams);
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
          problem: function() {
            return null;
          },
          setBaselineDistribution: function() {
            return function(baseline) {
              $scope.analysis.benefitRiskNMAOutcomeInclusions = $scope.analysis.benefitRiskNMAOutcomeInclusions.map(function(benefitRiskNMAOutcomeInclusion) {
                if (benefitRiskNMAOutcomeInclusion.outcomeId === outcomeWithAnalysis.outcome.id) {
                  return _.extend(benefitRiskNMAOutcomeInclusion, {
                    baseline: baseline
                  });
                } else {
                  return benefitRiskNMAOutcomeInclusion;
                }
              });
              var saveCommand = analysisToSaveCommand($scope.analysis);
              AnalysisResource.save(saveCommand).$save().then(function() {
                $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses($scope.analysis, $scope.outcomes, $scope.networkMetaAnalyses);
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
            $scope.outcomesWithAnalyses = BenefitRiskService.addScales($scope.outcomesWithAnalyses,
              includedAlternatives, result);
          }, function() {
            console.log('ScalesService.getObservedScales error');
          });
        }
      });
      $scope.isMissingBaseline = hasMissingBaseLine();
    }

  };
  return dependencies.concat(BenefitRiskStep2Controller);
});
