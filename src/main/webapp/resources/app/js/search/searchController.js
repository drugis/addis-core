'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$location', '$stateParams', 'SearchService'];
    var SearchController = function($scope, $location, $stateParams, SearchService) {
      // functions
      $scope.search = search;

      // init
      if ($stateParams.searchTerm) {
        $scope.searchTerm = $stateParams.searchTerm;
        $scope.searchPromise = SearchService.search($stateParams.searchTerm).then(function(results) {
          $scope.lastSearchTerm = $stateParams.searchTerm;
          $scope.searchResults = results.data;
        });
      }

      function search(searchTerm) {
        $location.search({
          searchTerm: searchTerm
        });
      }
    };
    return dependencies.concat(SearchController);
  });
