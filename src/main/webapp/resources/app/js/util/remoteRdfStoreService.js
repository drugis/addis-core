'use strict';
define([], function() {
  var dependencies = ['$http', 'UUIDService', 'FUSEKI_STORE_URL'];
  var RemotestoreService = function($http, 'UUIDService', FUSEKI_STORE_URL) {

  	var create = function() {
  		// gen uui for graph
  		var graphUUID = UUIDService.generate();
  		var query = 'CREATE GRAPH ' + uri ;
  		return execute(query);
  	}

  	var execute = function(query) {
  		return $http.post(FUSEKI_STORE_URL + '/query', query);
  	}

    return {
    	create: create,
    	execute: execute
    };
  };
  return dependencies.concat(RemotestoreService);
});
