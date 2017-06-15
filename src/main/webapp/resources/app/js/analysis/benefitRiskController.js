'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state', 'AnalysisResource', 'InterventionResource',
    'OutcomeResource', 'BenefitRiskService', 'ModelResource', 'ScenarioResource', 'UserService', 'ProjectResource',
    'SubProblemResource', 'DEFAULT_VIEW'
  ];
  var MetBenefitRiskController = function($scope, $q, $stateParams, $state, AnalysisResource, InterventionResource,
    OutcomeResource, BenefitRiskService, ModelResource, ScenarioResource, UserService, ProjectResource,
    SubProblemResource, DEFAULT_VIEW) {

    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.goToDefaultScenario = goToDefaultScenario;
    $scope.goToModel = goToModel;
    $scope.userId = $stateParams.userUid;
    $scope.project = ProjectResource.get($stateParams);

    $scope.editMode = {
      allowEditing: false
    };
    $scope.project.$promise.then(function() {
      if (UserService.isLoginUserId($scope.project.owner.id) && !$scope.analysis.archived) {
        $scope.editMode.allowEditing = true;
      }
    });

    var promises = [$scope.analysis.$promise, $scope.alternatives.$promise, $scope.outcomes.$promise, $scope.models.$promise, $scope.project.$promise];

    $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = result[3];
      var outcomeIds = outcomes.map(function(outcome) {
        return outcome.id;
      });
      if (UserService.isLoginUserId($stateParams.projectId)) {
        $scope.editMode.allowEditing = true;
      }
      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses = networkMetaAnalyses
          .map(_.partial(BenefitRiskService.joinModelsWithAnalysis, models))
          .map(BenefitRiskService.addModelsGroup);
        $scope.outcomesWithAnalyses = outcomes
          .map(_.partial(BenefitRiskService.buildOutcomeWithAnalyses, analysis, networkMetaAnalyses))
          .map(function(outcomeWithAnalysis) {
            outcomeWithAnalysis.networkMetaAnalyses = outcomeWithAnalysis.networkMetaAnalyses.sort(BenefitRiskService.compareAnalysesByModels);
            return outcomeWithAnalysis;
          });
        $scope.outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses);
        $scope.isMissingBaseline = _.find($scope.outcomesWithAnalyses, function(outcomeWithAnalysis) {
          return !outcomeWithAnalysis.baselineDistribution;
        });
      });

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = _.find(analysis.interventionInclusions, function(includedAlternative) {
          return includedAlternative.id === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });
      setIncludedAlternatives();

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

    function setIncludedAlternatives() {
      $scope.analysis.interventionInclusions = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
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

    function goToModel(model) {
      $state.go('model', {
        userUid: $scope.userId,
        projectId: $stateParams.projectId,
        analysisId: model.analysisId,
        modelId: model.id
      });
    }

  };
  return dependencies.concat(MetBenefitRiskController);
});
