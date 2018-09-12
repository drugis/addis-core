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
    'ProjectResource',
    'ProjectStudiesResource',
    'TrialverseResource',
    'UserService',
    'WorkspaceService'
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
    ProjectResource,
    ProjectStudiesResource,
    TrialverseResource,
    UserService,
    WorkspaceService
  ) {

    $scope.goToStep1 = goToStep1;
    $scope.openDistributionModal = openDistributionModal;

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.hasMissingBaseLine = hasMissingBaseLine;
    $scope.finalizeAndGoToDefaultScenario = finalizeAndGoToDefaultScenario;
    $scope.goToDefaultScenario = BenefitRiskService.goToDefaultScenario;
    $scope.project = ProjectResource.get($stateParams);
    $scope.userId = $stateParams.userUid;

    $scope.editMode = {
      allowEditing: false
    };
    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function() {
      if (!$scope.analysis.archived) {
        UserService.isLoginUserId($scope.project.owner.id).then(function(isOwner) {
          $scope.editMode.allowEditing = isOwner;
        });
      }
      $scope.projectVersionUuid = $scope.project.datasetVersion.split('/versions/')[1];
      TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      }).$promise.then(function(dataset) {
        $scope.datasetOwnerId = dataset.ownerId;
      });
    });
    var promises = [
      $scope.analysis.$promise,
      $scope.alternatives.$promise,
      $scope.outcomes.$promise,
      $scope.models.$promise,
      ProjectStudiesResource.query({
        projectId: $stateParams.projectId
      }).$promise
    ];

    $scope.step2Promise = $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = _.reject(result[3], 'archived');
      var studies = result[4];
      $scope.studiesWithUuid = _.map(studies, function(study) {
        return _.extend({}, study, {
          uuid: study.studyUri.split('/graphs/')[1]
        });
      });
      PageTitleService.setPageTitle('BenefitRiskStep2Controller', analysis.title + ' step 2');

      $scope.alternatives = addInclusionStatus(alternatives, analysis.interventionInclusions, 'interventionId');
      $scope.outcomes = addInclusionStatus(outcomes, analysis.benefitRiskNMAOutcomeInclusions, 'outcomeId');

      prepareEffectsTable(analysis, models);
    });

    function prepareEffectsTable(analysis, models) {
      var outcomeIds = _($scope.outcomes).filter('isIncluded').map('id').value();
      $scope.effectsTablePromise = AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        $scope.networkMetaAnalyses = filterArchivedAndAddModels(networkMetaAnalyses, models);

        analysis = BenefitRiskService.addModelBaseline(analysis, models, $scope.alternatives);
        var saveCommand = BenefitRiskService.analysisToSaveCommand($scope.analysis);
        return AnalysisResource.save(saveCommand).$promise.then(function() {
          return updateOutcomesWithAnalyses(analysis, $scope.outcomes);
        });
      });
    }

    function filterArchivedAndAddModels(networkMetaAnalyses, models) {
      return _(networkMetaAnalyses)
        .reject('archived')
        .map(_.partial(BenefitRiskService.joinModelsWithAnalysis, models))
        .map(BenefitRiskService.addModelsGroup)
        .value();
    }

    function updateOutcomesWithAnalyses(analysis, outcomes) {
      var outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(analysis, outcomes, $scope.networkMetaAnalyses);
      $scope.outcomesWithAnalyses = BenefitRiskService.addStudiesToOutcomes(
        outcomesWithAnalyses, analysis.benefitRiskStudyOutcomeInclusions, $scope.studiesWithUuid);
      return resetScales();
    }

    function hasMissingBaseLine() {
      return _.find($scope.outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return outcomeWithAnalysis.dataType === 'network' && !outcomeWithAnalysis.baselineDistribution;
      });
    }

    function addInclusionStatus(itemsToCheck, inclusions, property) {
      return itemsToCheck.map(function(item) {
        return _.extend({}, item, {
          isIncluded: !!_.find(inclusions, [property, item.id])
        });
      });
    }

    function finalizeAndGoToDefaultScenario() {
      $scope.analysis.finalized = true;
      BenefitRiskService.finalizeAndGoToDefaultScenario($scope.analysis);
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
              return _.partial(setBaseline, outcomeWithAnalysis);
            }
          }
        });
      });
    }

    function setBaseline(outcomeWithAnalysis, baseline) {
      $scope.analysis.benefitRiskNMAOutcomeInclusions = $scope.analysis.benefitRiskNMAOutcomeInclusions.map(function(benefitRiskNMAOutcomeInclusion) {
        if (benefitRiskNMAOutcomeInclusion.outcomeId === outcomeWithAnalysis.outcome.id) {
          return _.extend(benefitRiskNMAOutcomeInclusion, {
            baseline: baseline
          });
        } else {
          return benefitRiskNMAOutcomeInclusion;
        }
      });
      var saveCommand = BenefitRiskService.analysisToSaveCommand($scope.analysis);
      $scope.effectsTablePromise = AnalysisResource.save(saveCommand).$promise.then(function() {
        $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(
          $scope.analysis, $scope.outcomes, $scope.networkMetaAnalyses);
        $scope.outcomesWithAnalyses = BenefitRiskService.addStudiesToOutcomes(
          $scope.outcomesWithAnalyses, $scope.analysis.benefitRiskStudyOutcomeInclusions, $scope.studiesWithUuid);
        return resetScales();
      });
    }

    function resetScales() {
      return ProblemResource.get($stateParams).$promise.then(function(problem) {
        if (problem.performanceTable.length > 0) {
          return WorkspaceService.getObservedScales(problem).then(function(result) {
            var includedAlternatives = _.filter($scope.alternatives, function(alternative) {
              return alternative.isIncluded;
            });
            $scope.isMissingBaseline = hasMissingBaseLine();
            $scope.outcomesWithAnalyses = BenefitRiskService.addScales($scope.outcomesWithAnalyses,
              includedAlternatives, problem.criteria, result);
          }, function() {
            console.log('WorkspaceService.getObservedScales error');
          });
        }
      });
    }

  };
  return dependencies.concat(BenefitRiskStep2Controller);
});
