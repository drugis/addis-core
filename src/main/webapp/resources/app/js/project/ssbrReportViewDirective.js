'use strict';
define([], function() {
  var dependencies = ['ProblemResource', 'WorkspaceService', 'ProjectStudiesResource',
    'OutcomeResource', 'ScenarioResource', 'MCDAResultsService'];
  var SsbrReportViewDirective = function(ProblemResource, WorkspaceService, ProjectStudiesResource,
    OutcomeResource, ScenarioResource, MCDAResultsService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/project/ssbrReportView.html',
      scope: {
        user: '=',
        project: '=',
        analysis: '=',
        interventions: '='
      },
      link: function(scope) {

        function hasMissingPvfs(criteria) {
          return _.find(criteria, function(criterion){
            return !criterion.pvf;
          });
        }

        scope.problem = ProblemResource.get({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        }, function(problem) {
          problem.valueTree = WorkspaceService.buildValueTree(scope.problem);
          WorkspaceService.getObservedScales(scope.problem).then(function(scales) {
            scope.scales = scales;
          });
          return problem;
        });

        OutcomeResource.query({
          projectId: scope.project.id
        }).$promise.then(function(outcomes) {
          scope.outcomes = _.keyBy(outcomes, 'id');
        });

        if (scope.analysis.analysisType === 'Single-study Benefit-Risk') {
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
            if (!hasMissingPvfs(scenario.state.problem.criteria)) {
              scenario.state = MCDAResultsService.getResults(scope, scenario.state);
            }
            return scenario;
          });
        });

      }
    };
  };
  return dependencies.concat(SsbrReportViewDirective);
});
