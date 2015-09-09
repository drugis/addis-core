'use strict';
define([], function() {
  var dependencies = ['$window', '$state', '$stateParams', 'md5'];
  var searchResultDirective = function($window, $state, $stateParams, md5) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/search/searchResultDirective.html',
      scope: {
        result: '='
      },
      link: function(scope) {
        var studyRefParams = {
          userUid: scope.result.ownerUuid,
          datasetUUID: UUIDService.getUuid(scope.result.datasetUrl)
        };
      }
    };
  };
  return dependencies.concat(searchResultDirective);
});