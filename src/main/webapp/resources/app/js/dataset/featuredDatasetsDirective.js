'use strict';
define([], function() {
  var depencencies = ['DatasetResource', '$filter'];
  var FeaturedDatasetsDirective = function(DatasetResource, $filter) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/featuredDatasetsDirective.html',
      scope: {
        userUid: '=',
        createProjectDialog: '&'
      },
      link: function(scope) {
        scope.stripFrontFilter = $filter('stripFrontFilter');
        scope.isVisible = true;
        scope.featuredDatasets = [];
        scope.$watch('featuredDatasets', function(newSet, oldSet) {
          if (oldSet !== newSet) {
            var cols = $('.featured-dataset-col');
            $(cols[cols.length - 1]).addClass('end');
          }
        });

        DatasetResource.getFeatured({
          userUid: scope.userUid
        }).$promise.then(function(response) {
          scope.featuredDatasets = response;
        });

        scope.toggleVisibility = function() {
          scope.isVisible = !scope.isVisible;
        };

      }
    };
  };
  return depencencies.concat(FeaturedDatasetsDirective);
});
