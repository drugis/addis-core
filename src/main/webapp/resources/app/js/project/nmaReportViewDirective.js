'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$http', '$q', 'EvidenceTableResource', 'NetworkMetaAnalysisService',
    'ModelResource', 'AnalysisService', 'PataviTaskIdResource', 'PataviService',
    'ProblemResource'
  ];
  var nmaReportViewDirective = function($http, $q, EvidenceTableResource,
    NetworkMetaAnalysisService, ModelResource, AnalysisService,
    PataviTaskIdResource, PataviService, ProblemResource) {
    return {
      restrict: 'E',
      templateUrl: './nmaReportView.html',
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
            var momentSelections = NetworkMetaAnalysisService.buildMomentSelections(trialverseData, scope.analysis);
            scope.network = {
              network: NetworkMetaAnalysisService.transformTrialDataToNetwork(trialverseData, includedInterventions, scope.analysis, momentSelections)
            };
          });


        var modelsPromise = ModelResource.query({
          projectId: scope.project.id,
          analysisId: scope.analysis.id
        }).$promise;

        var primaryModelDefer = $q.defer();
        scope.primaryModelPromise = primaryModelDefer.promise;

        var resultsDefer = $q.defer();
        scope.resultsPromise = resultsDefer.promise;

        modelsPromise.then(function(models) {

          // only load problem if a model has been created, else it may not be possible to create a valid model
          if (models.length) {
            scope.problem = ProblemResource.get({
              analysisId: scope.analysis.id,
              projectId: scope.project.id
            });
          }

          scope.primaryModel = _.find(models, function(model) {
            return model.id === scope.analysis.primaryModel;
          });

          scope.otherModels = _.filter(models, function(model) {
            return model.id !== scope.analysis.primaryModel;
          });

          scope.showNoOtherUnarchived = _.reduce(scope.otherModels, function(accum, model) {
            return model.archived ? ++accum : accum;
           }, 0) > 0;

          if (scope.primaryModel) {
            primaryModelDefer.resolve(scope.primaryModel);
          }

        });

        scope.primaryModelPromise.then(function(model) {

          if (!model.taskUrl) {
            return;
          }

          PataviService.listen(model.taskUrl)
            .then(function(result) {
                scope.result = result;
                resultsDefer.resolve(result);
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
            typeString = 'evidence synthesis';
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
