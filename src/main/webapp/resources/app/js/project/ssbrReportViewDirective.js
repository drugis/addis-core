'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ProblemResource', 'WorkspaceService', 'ProjectStudiesResource',
    'OutcomeResource', 'AnalysisResource', 'ModelResource', 'ScenarioResource', 'MCDAResultsService', 'TaskDependencies'
  ];
  var SsbrReportViewDirective = function(ProblemResource, WorkspaceService, ProjectStudiesResource,
    OutcomeResource, AnalysisResource, ModelResource, ScenarioResource, MCDAResultsService, TaskDependencies) {
    return {
      restrict: 'E',
      templateUrl: './ssbrReportView.html',
      scope: {
        user: '=',
        project: '=',
        analysis: '=',
        interventions: '='
      },
      link: function(scope) {

        if(scope.analysis.problem) {
          scope.problem = scope.analysis.problem;
          scope.problem.valueTree = WorkspaceService.buildValueTree(scope.analysis.problem);
          WorkspaceService.getObservedScales(scope, scope.problem).then(function(scales) {
            scope.scales = scales;
          });
        }

        OutcomeResource.query({
          projectId: scope.project.id
        }).$promise.then(function(outcomes) {
          scope.outcomes = _.keyBy(outcomes, 'id');
        });

        AnalysisResource.query({
          projectId: scope.project.id
        }).$promise.then(function(analyses) {
          scope.analyses = _.keyBy(analyses, 'id');
          scope.models = {};
          analyses.forEach(function(analysis) {
            ModelResource.query({
              projectId: scope.project.id,
              analysisId: analysis.id
            }).$promise.then(function(models){
              scope.models = _.extend(scope.models, _.keyBy(models, 'id'));
            });
          });
        });

        if (scope.analysis.studyGraphUri) {
          ProjectStudiesResource.query({
            projectId: scope.project.id
          }).$promise.then(function(studies) {
            scope.dataSource = _.find(studies, function(study) {
              return study.studyUri === scope.analysis.studyGraphUri;
            });
            var uri = scope.dataSource.studyUri;
            scope.dataSource.uid = uri.slice(uri.lastIndexOf('/') + 1, uri.length);
          });
        }

        scope.scenarios = ScenarioResource.query({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        }).$promise.then(function(scenarios) {
          scope.scenarios = scenarios.map(function(scenario) {
            if (TaskDependencies.isAccessible({ requires: ['scale-range', 'partial-value-function']}, scenario.state).accessible) {
              scenario.state = MCDAResultsService.getResults(scope, scenario.state.problem);
            }
            return scenario;
          });
        });

      }
    };
  };
  return dependencies.concat(SsbrReportViewDirective);
});
