'use strict';
define(['lodash', 'angular'], function(_) {
  var dependencies = [
    '$scope',
    '$q',
    '$stateParams',
    '$state',
    'AnalysisResource',
    'BenefitRiskService',
    'BenefitRiskStep1Service',
    'InterventionResource',
    'ModelResource',
    'OutcomeResource',
    'PageTitleService',
    'ProjectResource',
    'ProjectStudiesResource',
    'SingleStudyBenefitRiskService',
    'UserService'
  ];
  var BenefitRiskStep1Controller = function(
    $scope,
    $q,
    $stateParams,
    $state,
    AnalysisResource,
    BenefitRiskService,
    BenefitRiskStep1Service,
    InterventionResource,
    ModelResource,
    OutcomeResource,
    PageTitleService,
    ProjectResource,
    ProjectStudiesResource,
    SingleStudyBenefitRiskService,
    UserService
  ) {
    // functions
    $scope.addedAlternative = addedAlternative;
    $scope.removedAlternative = removedAlternative;
    $scope.updateOutcomeInclusion = updateOutcomeInclusion;
    $scope.updateAnalysesInclusions = updateAnalysesInclusions;
    $scope.updateModelSelection = updateModelSelection;
    $scope.goToStep2 = goToStep2;
    $scope.saveInclusions = saveInclusions;
    $scope.finalizeAndGoToDefaultScenario = finalizeAndGoToDefaultScenario;
    $scope.checkStep1Validity = checkStep1Validity;

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
    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function() {
      if (!$scope.analysis.archived && !$scope.analysis.finalized) {
        UserService.isLoginUserId($scope.project.owner.id).then(function(isOwner) {
          $scope.editMode.allowEditing = isOwner;
        });
      }
    });

    var promises = [
      $scope.analysis.$promise,
      $scope.alternatives.$promise,
      $scope.outcomes.$promise,
      $scope.models.$promise,
      studiesPromise
    ];

    $scope.step1Promise = $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = _.reject(_.reject(result[3], 'archived'), function(model) {
        return model.likelihood === 'binom' && model.link === 'log';
      });
      var studies = result[4];

      var outcomeIds = _.map(outcomes, 'id');

      PageTitleService.setPageTitle('BenefitRiskStep1Controller', analysis.title + ' step 1');

      $scope.studies = studies;
      $scope.alternatives = alternatives;
      $scope.includedAlternatives = alternatives.filter(function(alternative) {
        return _.find(analysis.interventionInclusions, ['interventionId', alternative.id]);
      });
      var allInclusions = analysis.benefitRiskNMAOutcomeInclusions.concat(analysis.benefitRiskStudyOutcomeInclusions);
      $scope.outcomes = BenefitRiskService.getOutcomesWithInclusions(outcomes, allInclusions);

      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        $scope.outcomesWithAnalyses = BenefitRiskStep1Service.buildOutcomesWithAnalyses(
          analysis, studies, networkMetaAnalyses, models, $scope.outcomes
        );
        $scope.outcomesWithAnalyses = updateMissingAlternativesForAllOutcomes();
        updateStudyMissingStuff();
        checkStep1Validity();
      });
    });

    function goToStep2() {
      $state.go('BenefitRiskCreationStep-2', $stateParams);
    }

    function checkStep1Validity() {
      $scope.step1AlertMessages = BenefitRiskStep1Service.getStep1Errors($scope.outcomesWithAnalyses);
      if ($scope.includedAlternatives.length < 2) {
        $scope.step1AlertMessages.push('At least two alternatives must be selected');
      }
      if ($scope.overlappingInterventions.length > 0) {
        $scope.step1AlertMessages.push('There are overlapping interventions');
      }
      if ($scope.analysis.finalized) {
        $scope.step1AlertMessages.push('Analysis is already finalized');
      }
    }

    function updateAnalysesInclusions(outcome) {
      outcome.selectedModel = BenefitRiskStep1Service.getModelSelection(outcome.selectedAnalysis);
      if (outcome.selectedModel) {
        outcome.selectedModel = BenefitRiskStep1Service.updateMissingAlternatives(outcome, $scope.includedAlternatives);
      }
      saveInclusions();
    }

    function updateOutcomeInclusion(inclusion) {
      BenefitRiskStep1Service.updateOutcomeInclusion(inclusion, $scope.includedAlternatives);
      saveInclusions();
    }

    function updateModelSelection(outcome) {
      if (outcome.selectedModel) {
        outcome.selectedModel = BenefitRiskStep1Service.updateMissingAlternatives(outcome, $scope.includedAlternatives);
      }
      saveInclusions();
    }

    function updateMissingAlternativesForAllOutcomes() {
      return _.map($scope.outcomesWithAnalyses, function(outcome) {
        if (outcome.selectedModel) {
          outcome.selectedModel = BenefitRiskStep1Service.updateMissingAlternatives(outcome, $scope.includedAlternatives);
        }
        return outcome;
      });
    }

    function updateStudyMissingStuff() {
      $scope.studies = SingleStudyBenefitRiskService.getStudiesWithErrors($scope.studies, $scope.includedAlternatives);
      $scope.overlappingInterventions = BenefitRiskStep1Service.findOverlappingInterventions($scope.studies);
      $scope.outcomesWithAnalyses = _.map($scope.outcomesWithAnalyses, function(outcomeWithAnalyses) {
        if (!_.isEmpty(outcomeWithAnalyses.selectedStudy)) {
          outcomeWithAnalyses.selectedStudy = _.find($scope.studies, ['studyUri', outcomeWithAnalyses.selectedStudy.studyUri]);
          outcomeWithAnalyses.selectedStudy.missingOutcomes = SingleStudyBenefitRiskService.findMissingOutcomes(outcomeWithAnalyses.selectedStudy, [outcomeWithAnalyses]);
        }
        return outcomeWithAnalyses;
      });
      $scope.contrastStudySelected = BenefitRiskStep1Service.isContrastStudySelected($scope.outcomesWithAnalyses, $scope.studies);
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
      $scope.outcomesWithAnalyses = updateMissingAlternativesForAllOutcomes();
      updateStudyMissingStuff();
      var updateCommand = BenefitRiskStep1Service.analysisUpdateCommand($scope.analysis, $scope.includedAlternatives);
      AnalysisResource.save(updateCommand);
      checkStep1Validity();
    }

    function saveInclusions() {
      $scope.analysis.benefitRiskNMAOutcomeInclusions = BenefitRiskStep1Service.getNMAOutcomeInclusions($scope.outcomesWithAnalyses, $scope.analysis.id);
      $scope.analysis.benefitRiskStudyOutcomeInclusions = BenefitRiskStep1Service.getStudyOutcomeInclusions($scope.outcomesWithAnalyses, $scope.analysis.id);
      checkStep1Validity();
      updateStudyMissingStuff();
      var updateCommand = BenefitRiskStep1Service.analysisUpdateCommand($scope.analysis, $scope.includedAlternatives);
      AnalysisResource.save(updateCommand);
    }

    function finalizeAndGoToDefaultScenario() {
      $scope.analysis.finalized = true;
      BenefitRiskService.finalizeAndGoToDefaultScenario($scope.analysis);
    }

  };
  return dependencies.concat(BenefitRiskStep1Controller);
});
