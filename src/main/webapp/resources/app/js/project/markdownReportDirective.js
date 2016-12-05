'use strict';
define(['showdown'], function(Showdown) {
  var dependencies = ['$stateParams', '$compile', '$sanitize', 'ReportResource', 'ReportDirectiveService'];
  var MarkdownReportDirective = function($stateParams, $compile, $sanitize, ReportResource, ReportDirectiveService) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      templateUrl: 'app/js/project/markdownReportDirective.html',
      link: function(scope, element) {
        function updateHtml(data) {
          var html = $sanitize(converter.makeHtml(data));
          html = ReportDirectiveService.inlineDirectives(html);
          element.html(html);
          $compile(element.contents())(scope);
        }
        var converter = new Showdown.Converter();
        scope.reportText = ReportResource.get($stateParams);
        scope.$watch('data', function(newVal) {
          if (newVal) {
            updateHtml(newVal);
          }
        });
        scope.reportText.$promise.then(function(reportResponse) {
          updateHtml(reportResponse.data);
        });
      }
    };
  };
  return dependencies.concat(MarkdownReportDirective);
});
