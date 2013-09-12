'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
  controller('IndicationController', ['$scope', 'SparqlService', function($scope, $sparql) {
	$scope.query = function() {
		$sparql.query('disease-search', { query: $scope.searchString }).then(
			function(result) {
				$scope.data = result.data.results.bindings;
				$scope.sparql = result.query
			},
			function(error) {
				console.log(error.cause);
				if (error.query) {
					$scope.sparql = error.query;
				}
			});
	};
	$scope.sparql = '';
  }]);
