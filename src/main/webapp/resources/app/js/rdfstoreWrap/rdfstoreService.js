'use strict';
define(['angular', 'rdfstore'], function(angular, rdfstore) {
  var dependencies = ['$q'];
  var RdfstoreService = function($q) {

    function init(store) {
      var promiseHolder = $q.defer();
      if (!store || !store.load) {
        rdfstore.create(promiseHolder.resolve);
      } else {
        promiseHolder.resolve(store);
      }
      return promiseHolder;
    }

    function load(store, data) {
      var promiseHolder = $q.defer();
      init(store).promise.then(function(store) {
        store.load('application/ld+json', data, function() {
          promiseHolder.resolve(store);
        });
      });
      return promiseHolder;
    }

    function execute(store, query) {
      var promiseHolder = $q.defer();
      init(store).promise.then(function(store) {
        store.execute(query, function(success, result) {
          if (success) {
            promiseHolder.resolve(result);
          } else {
            console.error('failed to execute query ' + query);
            promiseHolder.reject('query failed ' + result);
          }
        });
      });
      return promiseHolder;
    }

    return {
      load: load,
      execute: execute
    };
  };
  return dependencies.concat(RdfstoreService);
});
