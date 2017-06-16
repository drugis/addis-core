'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state',
    'ProjectStudiesResource',
    'AnalysisResource', 'InterventionResource',
    'OutcomeResource', 'BenefitRiskService',
    'ModelResource', 'ProjectResource', 'UserService', 'SingleStudyBenefitRiskService'
  ];
  var MetBenefitRiskStep1Controller = function($scope, $q, $stateParams, $state,
    ProjectStudiesResource,
    AnalysisResource, InterventionResource,
    OutcomeResource, BenefitRiskService,
    ModelResource, ProjectResource, UserService, SingleStudyBenefitRiskService) {
    // functions
    $scope.addedAlternative = addedAlternative;
    $scope.removedAlternative = removedAlternative;
    $scope.updateBenefitRiskNMAOutcomeInclusions = updateBenefitRiskNMAOutcomeInclusions;
    $scope.updateAnalysesInclusions = updateAnalysesInclusions;
    $scope.isOutcomeDisabled = isOutcomeDisabled;
    $scope.updateModelSelection = updateModelSelection;
    $scope.goToStep2 = goToStep2;
    $scope.selectAllOutcomes = selectAllOutcomes;
    $scope.deselectAllOutcomes = deselectAllOutcomes;

    // init
    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.project = ProjectResource.get($stateParams);
    var studiesPromise = ProjectStudiesResource.query({
      projectId: $stateParams.projectId
    }).$promise;
    $scope.userId = $stateParams.userUid;

    $scope.editMode = {
      allowEditing: false
    };
    $scope.project.$promise.then(function() {
      if (UserService.isLoginUserId($scope.project.owner.id) && !$scope.analysis.archived) {
        $scope.editMode.allowEditing = true;
      }
    });

    function goToStep2() {
      $state.go('BenefitRiskCreationStep-2', $stateParams);
    }

    /*
     ** (1) two or more interventions have been selected,
     ** (2) two or more outcomes have been selected,
     ** (3) for each outcome, an analysis and model have been selected,
     **     and the selected model includes all selected interventions and has results
     */
    function checkStep1Validity() {
      $scope.step1AlertMessages = [];
      //(1)
      if ($scope.includedAlternatives.length < 2) {
        $scope.step1AlertMessages.push('At least two alternatives must be selected.');
      }
      //(2)
      var numberOfSelectedOutcomes = BenefitRiskService.numberOfSelectedOutcomes($scope.outcomesWithAnalyses);
      if (numberOfSelectedOutcomes < 2) {
        $scope.step1AlertMessages.push('At least two outcomes must be selected.');
      }
      //(3)
      var isModelWithMissingAlternatives = BenefitRiskService.isModelWithMissingAlternatives($scope.outcomesWithAnalyses);
      if (isModelWithMissingAlternatives) {
        $scope.step1AlertMessages.push('A model with missing alternatives is selected');
      }
      //(3)
      var isModelWithoutResults = BenefitRiskService.isModelWithoutResults($scope.outcomesWithAnalyses);
      if (isModelWithoutResults) {
        $scope.step1AlertMessages.push('A model that has not yet run is selected');
      }
    }

    function updateAnalysesInclusions(changedOutcome) {
      changeModelSelection(changedOutcome);
      if (changedOutcome.selectedModel) {
        updateMissingAlternatives(changedOutcome);
      }
      buildInclusions();
    }

    function updateBenefitRiskNMAOutcomeInclusions(changedOutcome) {
      if (hasSelectableAnalysis(changedOutcome) && changedOutcome.outcome.isIncluded) {
        changedOutcome.selectedAnalysis = changedOutcome.networkMetaAnalyses[0];
      } else {
        changedOutcome.selectedAnalysis = undefined;
      }
      updateAnalysesInclusions(changedOutcome);
    }

    function updateModelSelection(owa) {
      if (owa.selectedModel) {
        updateMissingAlternatives(owa);
      }
      buildInclusions();
    }

    var promises = [$scope.analysis.$promise, $scope.alternatives.$promise, $scope.outcomes.$promise, $scope.models.$promise,
      studiesPromise
    ];

    $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = _.reject(result[3], 'archived');
      var studies = result[4];

      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });

      $scope.studies = studies;
      $scope.studyArrayLength = studies.length;

      // $scope.studyModel.selectedStudy = _.find(studies, function(study) {
      //   return study.studyUri === $scope.analysis.studyGraphUri;
      // });
      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses = networkMetaAnalyses
          .filter(function(analysis) {
            return !analysis.archived;
          })
          .map(_.partial(BenefitRiskService.joinModelsWithAnalysis, models))
          .map(BenefitRiskService.addModelsGroup);
        $scope.outcomesWithAnalyses = outcomes
          .map(_.partial(BenefitRiskService.buildOutcomeWithAnalyses, analysis, networkMetaAnalyses))
          .map(function(owa) {
            owa.networkMetaAnalyses = owa.networkMetaAnalyses.sort(BenefitRiskService.compareAnalysesByModels);
            return owa;
          });
        updateMissingAlternativesForAllOutcomes();

        updateStudyMissingStuff();
        // when view setup is completed
        checkStep1Validity();
      });

      $scope.alternatives = alternatives;

      $scope.includedAlternatives = alternatives.filter(function(alternative) {
        return analysis.interventionInclusions.find(function(includedAlternative) {
          return includedAlternative.interventionId === alternative.id;
        });
      });

      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = analysis.benefitRiskNMAOutcomeInclusions.find(function(benefitRiskNMAOutcomeInclusion) {
          return benefitRiskNMAOutcomeInclusion.outcomeId === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
    });

    function updateStudyMissingStuff() {
      $scope.studies = SingleStudyBenefitRiskService.addMissingOutcomesToStudies($scope.studies, $scope.outcomesWithAnalyses);
      $scope.studies = SingleStudyBenefitRiskService.addMissingInterventionsToStudies($scope.studies, $scope.includedAlternatives);
      SingleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.alternativeInclusions);
      $scope.studies = SingleStudyBenefitRiskService.addOverlappingInterventionsToStudies($scope.studies, $scope.alternativeInclusions);
      SingleStudyBenefitRiskService.recalculateGroup($scope.studies);
    }

    function isOutcomeDisabled(outcomeWithAnalyses) {
      return !outcomeWithAnalyses.networkMetaAnalyses.length ||
        !hasSelectableAnalysis(outcomeWithAnalyses);
    }

    function updateMissingAlternativesForAllOutcomes() {
      $scope.outcomesWithAnalyses.filter(function(outcome) {
        return outcome.selectedModel;
      }).forEach(updateMissingAlternatives);
    }

    function addedAlternative(alternative) {
      $scope.includedAlternatives.push(alternative);
      updateAlternatives();
    }

    function removedAlternative(alternative) {
      $scope.includedAlternatives.splice($scope.includedAlternatives.indexOf(alternative), 1);
      updateAlternatives();
    }

    function updateAlternatives() {
      updateMissingAlternativesForAllOutcomes();
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand);
      updateStudyMissingStuff();
      checkStep1Validity();
    }

    function selectAllOutcomes() {
      $scope.outcomesWithAnalyses = $scope.outcomesWithAnalyses.map(function(owa) {
        owa.isIncluded = true;
        return owa;
      });
      updateAllOutcomeInclusions();
    }

    function deselectAllOutcomes() {
      $scope.outcomesWithAnalyses = $scope.outcomesWithAnalyses.map(function(owa) {
        owa.isIncluded = false;
        return owa;
      });
      buildInclusions();
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

    function updateMissingAlternatives(owa) {
      owa.selectedModel.missingAlternatives = BenefitRiskService.findMissingAlternatives($scope.includedAlternatives, owa);
      owa.selectedModel.missingAlternativesNames = _.map(owa.selectedModel.missingAlternatives, 'name');
    }

    function analysisToSaveCommand(analysis) {
      var analysisToSave = angular.copy(analysis);
      analysisToSave.interventionInclusions = $scope.includedAlternatives.map(function(intervention) {
        return {
          interventionId: intervention.id,
          analysisId: analysisToSave.id
        };
      });
      return {
        id: analysis.id,
        projectId: analysis.projectId,
        analysis: analysisToSave
      };
    }

    function buildInclusions() {
      $scope.analysis.benefitRiskNMAOutcomeInclusions = $scope.outcomesWithAnalyses.filter(function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.outcome.isIncluded;
      }).map(function(outcomeWithAnalyses) {
        return {
          analysisId: $scope.analysis.id,
          outcomeId: outcomeWithAnalyses.outcome.id,
          networkMetaAnalysisId: outcomeWithAnalyses.selectedAnalysis.id,
          modelId: outcomeWithAnalyses.selectedModel.id
        };
      });
      checkStep1Validity();
      updateStudyMissingStuff();
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand);
    }

  };
  return dependencies.concat(MetBenefitRiskStep1Controller);
});
