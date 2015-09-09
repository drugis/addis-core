'use strict';
define([],
  function() {
    var dependencies = ['$scope', 'SearchService'];
    var SearchController = function($scope, SearchService) {

      $scope.search = search;

      function search(searchTerm) {
        $scope.searchResults = undefined;
        SearchService.search(searchTerm).then(function (results){
          $scope.searchResults = results.data;
        });
      }
    };
    return dependencies.concat(SearchController);
  });
