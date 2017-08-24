'use strict';
define(['showdown'], function(Showdown) {
  var dependencies = ['$stateParams', '$q', '$compile', '$sanitize', 'ReportResource', 'ReportDirectiveService', 'DefaultReportService'];
  var MarkdownReportDirective = function($stateParams, $q, $compile, $sanitize, ReportResource, ReportDirectiveService, DefaultReportService) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      templateUrl: 'app/js/project/report/markdownReportDirective.html',
      link: function(scope, element) {
        scope.loading = {
          loaded: false
        };

        function updateHtml(data) {
          var html = $sanitize(converter.makeHtml(data));
          html = ReportDirectiveService.inlineDirectives(html);
          element.html(html);
          $compile(element.contents())(scope);
        }

        var converter = new Showdown.Converter();
        ReportResource.get($stateParams).$promise.then(function(reportResponse) {
          var report = reportResponse.data;
          if (report === 'default report text') {
            DefaultReportService.generateDefaultReport($stateParams.projectId).then(function(defaultReport) {
              scope.reportText = defaultReport;
              updateHtml(defaultReport);
              scope.loading.loaded = true;
            });
          } else {
            scope.reportText = report;
            updateHtml(report);
            scope.loading.loaded = true;
          }
        });
        scope.$watch('data', function(newVal) {
          if (newVal) {
            updateHtml(newVal);
          }
        });
      }
    };
  };
  return dependencies.concat(MarkdownReportDirective);
});
