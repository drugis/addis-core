'use strict';
define([], function() {
  var depencencies = ['DatasetResource', '$filter', 'UserService'];
  var FeaturedDatasetsDirective = function(DatasetResource, $filter, UserService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/featuredDatasetsDirective.html',
      scope: {
        createProjectDialog: '&'
      },
      link: function(scope) {
        scope.stripFrontFilter = $filter('stripFrontFilter');
        scope.isVisible = true;
        scope.showCreateProjectButton = UserService.hasLoggedInUser();
        scope.featuredDatasets = [];
        DatasetResource.getFeatured().$promise.then(function(response) {
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
