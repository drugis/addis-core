'use strict';
define(['showdown'], function(Showdown) {
  var dependencies = ['$stateParams', '$q', '$compile', '$sanitize', 'ReportResource', 'ReportDirectiveService', 'DefaultReportService'];
  var MarkdownReportDirective = function($stateParams, $q, $compile, $sanitize, ReportResource, ReportDirectiveService, DefaultReportService) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      templateUrl: './markdownReportDirective.html',
      link: function(scope, element) {
        var converter = new Showdown.Converter();
        scope.reportPromise = ReportResource.get($stateParams).$promise.then(function(reportResponse) {
          var report = reportResponse.data;
          if (report === 'default report text') {
            DefaultReportService.generateDefaultReport($stateParams.projectId).then(function(defaultReport) {
              scope.reportText = defaultReport;
              updateHtml(defaultReport);
            });
          } else {
            scope.reportText = report;
            updateHtml(report);
          }
        });
        scope.$watch('data', function(newVal) {
          if (newVal) {
            updateHtml(newVal);
          }
        });

        function updateHtml(data) {
          var html = $sanitize(converter.makeHtml(data));
          html = ReportDirectiveService.inlineDirectives(html);
          element.html(html);
          $compile(element.contents())(scope);
        }
      }
    };
  };
  return dependencies.concat(MarkdownReportDirective);
});
