'use strict';
define([],
  function() {
    var dependencies = ['$http'];
    var SearchService = function($http) {

      function search(searchTerm) {
        return $http.get('/search', {
          params: {
            searchTerm: searchTerm
          }
        });
      }

      return {
        search: search,
      };
    };

    return dependencies.concat(SearchService);
  });