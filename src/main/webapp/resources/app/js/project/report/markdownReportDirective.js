'use strict';
define(['showdown'], function(Showdown) {
  var dependencies = ['$stateParams', '$compile', '$sanitize', 'ReportResource', 'ReportDirectiveService'];
  var MarkdownReportDirective = function($stateParams, $compile, $sanitize, ReportResource, ReportDirectiveService) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      templateUrl: 'app/js/project/report/markdownReportDirective.html',
      link: function(scope, element) {
        function updateHtml(data) {
          var html = $sanitize(converter.makeHtml(data));
          html = ReportDirectiveService.inlineDirectives(html);
          element.html(html);
          $compile(element.contents())(scope);
        }
        var converter = new Showdown.Converter();
        ReportResource.get($stateParams).$promise.then(function(reportResponse) {
          scope.reportText = reportResponse.data;
          updateHtml(reportResponse.data);
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
