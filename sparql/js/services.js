'use strict';

angular.module('myApp.services', [])
	.service('SparqlService', ['$q', '$http', function($q, $http) {
		var runQuery = function(sparql) {
			return $http.get('http://localhost:3030/ds/query', {params: {'query': sparql, 'output': 'json'}, headers: {'Accept': 'application/json'}});
		};

		var getTemplate = function(name) {
			return $http.get('sparql/' + name + ".sparql", {headers: {'Accept': 'text/plain'}, cache: true});
		};

		this.query = function(templateName, vars) {
			var promise = $q.defer();
			getTemplate(templateName).success(function(template) {
				var query = _.template(template, vars);
				runQuery(query).success(function(results) { 
					promise.resolve({ query: query, data: results });
				}).error(function(value) { 
					promise.reject({ query: query, cause: value });
				});
			}).error(function(value) {
				promise.reject({ cause: value });
			});
			return promise.promise;
		};
	}]);
