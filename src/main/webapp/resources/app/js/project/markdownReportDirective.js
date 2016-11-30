'use strict';
define(['showdown'], function(Showdown) {
  var dependencies = ['$stateParams', '$compile', '$sanitize', 'ReportResource'];
  var MarkdownReportDirective = function($stateParams, $compile, $sanitize, ReportResource) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      templateUrl: 'app/js/project/markdownReportDirective.html',
      link: function(scope, element) {
        function updateHtml(data) {
          var html = $sanitize(converter.makeHtml(data));
          element.html(html);
          $compile(element.contents())(scope);
        }
        var converter = new Showdown.Converter();
        scope.reportText = ReportResource.get($stateParams);
        if (scope.data) {
          scope.$watch('data', function(newVal, oldVal) {
            if (newVal) {
              updateHtml(newVal);
            }
          });
        } else {
          scope.reportText.$promise.then(function(reportResponse) {
            updateHtml(reportResponse.data);
          });
        }
      }
    };
  };
  return dependencies.concat(MarkdownReportDirective);
});
