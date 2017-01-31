'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$state', 'AnalysisResource', 'InterventionResource',
    'OutcomeResource', 'MetaBenefitRiskService', 'ModelResource', 'ScenarioResource', 'UserService', 'ProjectResource'
  ];
  var MetBenefitRiskController = function($scope, $q, $stateParams, $state, AnalysisResource, InterventionResource,
    OutcomeResource, MetaBenefitRiskService, ModelResource, ScenarioResource, UserService, ProjectResource) {

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
      if (UserService.isLoginUserId($scope.project.owner.id)) {
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
          .map(_.partial(MetaBenefitRiskService.joinModelsWithAnalysis, models))
          .map(MetaBenefitRiskService.addModelsGroup);
        $scope.outcomesWithAnalyses = outcomes
          .map(_.partial(MetaBenefitRiskService.buildOutcomesWithAnalyses, analysis, networkMetaAnalyses, models))
          .map(function(owa) {
            owa.networkMetaAnalyses = owa.networkMetaAnalyses.sort(MetaBenefitRiskService.compareAnalysesByModels);
            return owa;
          });
        $scope.outcomesWithAnalyses = buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses, models);
        $scope.isMissingBaseline = _.find($scope.outcomesWithAnalyses, function(owa) {
          return !owa.baselineDistribution;
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
        var isOutcomeInInclusions = _.find(analysis.mbrOutcomeInclusions, function(mbrOutcomeInclusion) {
          return mbrOutcomeInclusion.outcomeId === outcome.id;
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

    function goToDefaultScenario() {
      ScenarioResource
        .query(_.omit($stateParams, 'id'))
        .$promise
        .then(function(scenarios) {
          $state.go('overview', {
            userUid: $scope.userId,
            projectId: $stateParams.projectId,
            analysisId: $stateParams.analysisId,
            id: scenarios[0].id
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
