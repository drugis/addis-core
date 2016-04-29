'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'EvidenceTableResource', 'NetworkMetaAnalysisService',
    'ModelResource', 'AnalysisService', 'PataviTaskIdResource', 'PataviService',
    'ProblemResource'
  ];
  var nmaReportViewDirective = function($q, EvidenceTableResource,
    NetworkMetaAnalysisService, ModelResource, AnalysisService,
    PataviTaskIdResource, PataviService, ProblemResource) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/project/nmaReportView.html',
      scope: {
        user: '=',
        project: '=',
        analysis: '=',
        interventions: '='
      },
      link: function(scope) {
        EvidenceTableResource.query({
          projectId: scope.project.id,
          analysisId: scope.analysis.id
        })
          .$promise.then(function(trialverseData) {
            scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions(scope.interventions, scope.analysis.interventionInclusions);
            var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions(scope.interventions);
            scope.network = NetworkMetaAnalysisService.transformTrialDataToNetwork(trialverseData, includedInterventions, scope.analysis);
          });


        var modelsPromise = ModelResource.query({
          projectId: scope.project.id,
          analysisId: scope.analysis.id
        }).$promise;

        scope.problem = ProblemResource.get({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        });

        var primaryModelDefer = $q.defer();
        scope.primaryModelPromise = primaryModelDefer.promise;

        var resultsDefer = $q.defer();
        scope.resultsPromise = resultsDefer.promise;

        modelsPromise.then(function(models) {
          scope.primaryModel = _.find(models, function(model) {
            return model.id === scope.analysis.primaryModel;
          });

          scope.otherModels = _.filter(models, function(model) {
            return model.id !== scope.analysis.primaryModel;
          });

          if (scope.primaryModel) {
            primaryModelDefer.resolve(scope.primaryModel);
          } else {
            primaryModelDefer.reject('no primary model had been set');
          }

        });

        scope.primaryModelPromise.then(function(model) {

          PataviTaskIdResource.get({
            projectId: scope.project.id,
            analysisId: scope.analysis.id,
            modelId: model.id
          })
            .$promise
            .then(PataviService.run)
            .then(function(result) {
                scope.result = result.results;
                resultsDefer.resolve(result)
              },
              function(pataviError) {
                console.error('an error has occurred, error: ' + JSON.stringify(pataviError));
                scope.$emit('error', {
                  type: 'patavi',
                  message: pataviError.desc
                });
              });
        });

        scope.modelSettingSummary = function(model) {
          if (!model) {
            return '';
          }
          var modelPart = model.linearModel === 'fixed' ? 'fixed effect' : 'random effects';
          var typePart = modelTypeString(model.modelType.type);
          var scalePart = AnalysisService.getScaleName(model);
          return modelPart + ' ' + typePart + ' on the ' + scalePart + ' scale.';
        };


        function modelTypeString(type) {
          var typeString;
          if (type === 'network') {
            typeString = 'network meta-analysis';
          } else if (type === 'pair-wise') {
            typeString = 'pair-wise meta-analysis';
          } else if (type === 'node-split') {
            typeString = 'node-splitting analysis';
          } else if (type === 'regression') {
            typeString = 'meta-regression';
          }
          return typeString;
        }


      }
    };
  };
  return dependencies.concat(nmaReportViewDirective);
});
