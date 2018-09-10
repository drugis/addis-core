'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    '$scope', '$q', '$stateParams', '$state', '$modal',
    'AnalysisResource',
    'BenefitRiskService',
    'InterventionResource',
    'ModelResource',
    'OutcomeResource',
    'PageTitleService',
    'ProblemResource',
    'ScenarioResource',
    'ProjectResource',
    'ProjectStudiesResource',
    'TrialverseResource',
    'UserService',
    'WorkspaceService',
    'DEFAULT_VIEW'
  ];
  var BenefitRiskStep2Controller = function(
    $scope, $q, $stateParams, $state, $modal,
    AnalysisResource,
    BenefitRiskService,
    InterventionResource,
    ModelResource,
    OutcomeResource,
    PageTitleService,
    ProblemResource,
    ScenarioResource,
    ProjectResource,
    ProjectStudiesResource,
    TrialverseResource,
    UserService,
    WorkspaceService,
    DEFAULT_VIEW
  ) {

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
      $scope.projectVersionUuid = $scope.project.datasetVersion.split('/versions/')[1];
      TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      }).$promise.then(function(dataset) {
        $scope.datasetOwnerId = dataset.ownerId;
      });
    });
    var promises = [$scope.analysis.$promise, $scope.alternatives.$promise, $scope.outcomes.$promise, $scope.models.$promise];

    $scope.step2Promise = $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = _.reject(result[3], 'archived');

      PageTitleService.setPageTitle('BenefitRiskStep2Controller', analysis.title+ ' step 2');

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

      $scope.effectsTablePromise = AnalysisResource.query({
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
        return AnalysisResource.save(saveCommand).$promise.then(function() {
          $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(analysis, outcomes, filteredNetworkMetaAnalyses);
          return ProjectStudiesResource.query({
            projectId: $stateParams.projectId
          }).$promise.then(function(studies) {
            $scope.studiesWithUuid = _.map(studies, function(study) {
              return _.extend({}, study, {
                uuid: study.studyUri.split('/graphs/')[1]
              });
            });
            $scope.outcomesWithAnalyses = BenefitRiskService.addStudiesToOutcomes(
              $scope.outcomesWithAnalyses, analysis.benefitRiskStudyOutcomeInclusions, $scope.studiesWithUuid);
            return resetScales();
          });
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
    });

    function addModelBaseline(analysis, models) {
      return BenefitRiskService.addModelBaseline(analysis, models, $scope.alternatives);
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
      var problem = null;
      ProblemResource.get({
        analysisId: outcomeWithAnalysis.selectedAnalysis.id,
        projectId: $stateParams.projectId
      }).$promise.then(function(result) {
        problem = result;
        $modal.open({
          templateUrl: 'gemtc-web/js/models/setBaselineDistribution.html',
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
              return problem;
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
                $scope.effectsTablePromise = AnalysisResource.save(saveCommand).$save().then(function() {
                  $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(
                    $scope.analysis, $scope.outcomes, $scope.networkMetaAnalyses);
                  $scope.outcomesWithAnalyses = BenefitRiskService.addStudiesToOutcomes(
                    $scope.outcomesWithAnalyses, $scope.analysis.benefitRiskStudyOutcomeInclusions, $scope.studiesWithUuid);
                  return resetScales();
                });
              };
            }
          }
        });
      });
    }

    function resetScales() {
      var problem;
      return ProblemResource.get($stateParams).$promise.then(function(problemResult) {
        problem = problemResult;
        if (problem.performanceTable.length > 0) {
          return WorkspaceService.getObservedScales(problem).then(function (result) {
            var includedAlternatives = _.filter($scope.alternatives, function (alternative) {
              return alternative.isIncluded;
            });
            $scope.isMissingBaseline = hasMissingBaseLine();
            $scope.outcomesWithAnalyses = BenefitRiskService.addScales($scope.outcomesWithAnalyses,
              includedAlternatives, problem.criteria, result);
          }, function () {
            console.log('WorkspaceService.getObservedScales error');
          });
        }
      });
    }

  };
  return dependencies.concat(BenefitRiskStep2Controller);
});
