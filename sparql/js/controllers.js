'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
controller('IndicationController', ['$scope', 'SparqlService', function($scope, $sparql) {
  $scope.selected = [];
  $scope.searchString = "cardio*";

  $scope.query = function() {
    $sparql.query('disease-search', { query: $scope.searchString }).then(
      function(result) {
        $scope.data = result.data.results.bindings;
        $scope.terms = $scope.data;
        $scope.narrower = [];
        $scope.broader = [];
        $scope.sparql = result.query;
      },
      function(error) {
        console.log(error.cause);
        if (error.query) {
          $scope.sparql = error.query;
        }
      });
  };

  $scope.getAdjacent = function(term) { 
    $scope.clicked = term;
    var uri = term.uri.value;
    var resultsFn = function(assignTo) {
      return function(result) {
        $scope.sparql = result.query;
        $scope.data = result.data.results.bindings;
        $scope[assignTo] = $scope.data;
      };
    };
    var errorFn = function(result) {
      $scope.sparql = result.query;
    };
    var broader = $sparql.query("disease-broader", { uri: uri }).then(resultsFn("broader"), errorFn);
    var narrower = $sparql.query("disease-narrower", { uri: uri }).then(resultsFn("narrower"), errorFn);
  };

  $scope.setCurrent = function(term) {
    $scope.terms = [term];
    $scope.getAdjacent(term);
  };

  $scope.add = function(term) {
    if (!_.contains($scope.selected, term)) {
      $scope.selected.push(term);
    }
  };

  $scope.remove = function(term) {
    var idx = _.indexOf($scope.selected, term);
    if (idx != -1) {
      $scope.selected.splice(idx, 1);
    }
  };
}]);

