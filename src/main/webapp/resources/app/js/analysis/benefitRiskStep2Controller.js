'use strict';
define(['lodash', 'angular'], function(_) {
  var dependencies = [
    '$scope',
    '$q',
    '$stateParams',
    '$state',
    '$modal',
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
    $scope,
    $q,
    $stateParams,
    $state,
    $modal,
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
    // functions
    $scope.goToStep1 = goToStep1;
    $scope.openDistributionModal = openDistributionModal;
    $scope.openStudyBaselineModal = openStudyBaselineModal;

    // init
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

      $scope.alternatives = addAlternativeInclusionStatus(alternatives, analysis.interventionInclusions);
      $scope.outcomes = BenefitRiskService.getOutcomesWithInclusions(outcomes, analysis);
      $scope.effectsTablePromise = prepareEffectsTable(analysis, models);
    });

    function prepareEffectsTable(analysis, models) {
      return BenefitRiskService.prepareEffectsTable($scope.outcomes)
        .then(function(networkMetaAnalyses) {
          $scope.networkMetaAnalyses = BenefitRiskService.filterArchivedAndAddModels(networkMetaAnalyses, models);
          var analysisWithBaselines = BenefitRiskService.addBaseline(analysis, models, $scope.alternatives);
          var saveCommand = BenefitRiskService.analysisToSaveCommand(analysisWithBaselines);
          return AnalysisResource.save(saveCommand).$promise.then(function() {
            return updateTableOutcomes(analysisWithBaselines, $scope.outcomes);
          });
        });
    }

    function updateTableOutcomes(analysis, outcomes) {
      $scope.tableOutcomes = BenefitRiskService.buildOutcomes(analysis, outcomes, $scope.networkMetaAnalyses, $scope.studiesWithUuid);
      return resetScales();
    }

    function addAlternativeInclusionStatus(alternatives, includedInterventions) {
      return alternatives.map(function(item) {
        return _.extend({}, item, {
          isIncluded: _.some(includedInterventions, ['interventionId', item.id])
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
      $scope.analysis.benefitRiskNMAOutcomeInclusions =
        addBaseline(outcomeWithAnalysis, $scope.analysis.benefitRiskNMAOutcomeInclusions, baseline);
      var saveCommand = BenefitRiskService.analysisToSaveCommand($scope.analysis);
      $scope.effectsTablePromise = AnalysisResource.save(saveCommand).$promise.then(function() {
        return updateTableOutcomes($scope.analysis, $scope.outcomes);
      });
    }

    function openStudyBaselineModal(outcome) {
      $modal.open({
        templateUrl: './setStudyBaseline.html',
        controller: 'SetStudyBaselineController',
        windowClass: 'small',
        resolve: {
          outcome: function() {
            return outcome;
          },
          measurementType: function() {
            return getMeasurementType(outcome);
          },
          referenceAlternativeName: function() {
            return getReferenceAlternativeName(outcome);
          },
          callback: function() {
            return _.partial(addStudyBaseline, outcome);
          }
        }
      });
    }

    function addStudyBaseline(outcome, baseline) {
      $scope.analysis.benefitRiskStudyOutcomeInclusions =
        addBaseline(outcome, $scope.analysis.benefitRiskStudyOutcomeInclusions, baseline);
      var saveCommand = BenefitRiskService.analysisToSaveCommand($scope.analysis);
      $scope.effectsTablePromise = AnalysisResource.save(saveCommand).$promise.then(function() {
        return updateTableOutcomes($scope.analysis, $scope.outcomes);
      });
    }

    function addBaseline(outcome, inclusions, baseline) {
      return _.map(
        inclusions, function(inclusion) {
          if (inclusion.outcomeId === outcome.outcome.id) {
            return _.extend(inclusion, { baseline: baseline });
          } else {
            return inclusion;
          }
        }
      );
    }

    function getReferenceAlternativeName(outcome) {
      var referenceArm = _.find(outcome.selectedStudy.arms, function(arm) {
        return arm.referenceArm === arm.uri;
      });
      var referenceAlternativeId = referenceArm.matchedProjectInterventionIds[0];
      var referenceAlternative = _.find($scope.alternatives, function(alternative) {
        return alternative.id === referenceAlternativeId;
      });
      return referenceAlternative.name;
    }

    function getMeasurementType(outcome) {
      var study = outcome.selectedStudy;
      var nonReferenceArm = _.find(study.arms, function(arm) {
        return arm.referenceArm !== arm.uri;
      });
      var measurement = nonReferenceArm.measurements[study.defaultMeasurementMoment][0];
      if (measurement.oddsRatio !== undefined) {
        return 'oddsRatio';
      } else if (measurement.riskRatio !== undefined) {
        return 'riskRatio';
      } else if (measurement.meanDifference !== undefined) {
        return 'meanDifference';
      } else if (measurement.standardizedMeanDifference !== undefined) {
        return 'standardizedMeanDifference';
      } else if (measurement.hazardRatio !== undefined) {
        return 'hazardRatio';
      }
    }

    function resetScales() {
      return ProblemResource.get($stateParams).$promise.then(function(problem) {
        if (problem.performanceTable.length > 0) {
          return WorkspaceService.getObservedScales(problem).then(function(result) {
            $scope.isMissingBaseline = hasMissingBaseLine();
            $scope.tableOutcomes = BenefitRiskService.addScales($scope.tableOutcomes,
              $scope.alternatives, problem.criteria, result);
          }, function() {
            console.log('WorkspaceService.getObservedScales error');
          });
        }
      });
    }

    function hasMissingBaseLine() {
      return _.some($scope.updateTableOutcomes, function(outcome) {
        return outcome.dataType === 'network' && !outcome.baseline ||
          outcome.dataType === 'single-study' && outcome.isContrastOutcome && !outcome.baseline;
      });
    }
  };
  return dependencies.concat(BenefitRiskStep2Controller);
});
