'use strict';
define([], function() {
  var dependencies = ['$q', 'DatasetService'];
  var DatasetOverviewService = function($q, DatasetService) {

    var loadDefer = $q.defer();
    var scratchUri;

    function loadStore(data) {
      return DatasetService.loadStore(data, function(newGraphUri) {
        scratchUri = newGraphUri;
      }).then(function() {
        loadDefer.resolve();
      });
    }

    function queryDatasetsOverview() {
      return loadDefer.promise.then(function() {
        var query =
          ' PREFIX dcterms: <http://purl.org/dc/terms/> ' +
          ' PREFIX void: <http://rdfs.org/ns/void#> ' +
          ' PREFIX es: <http://drugis.org/eventSourcing/es#> ' +
          ' SELECT ' +
          ' ?datasetUri ?label ?comment ?headVersion ' +
          ' WHERE { graph <' + scratchUri + '> {' +
          '   ?datasetUri ' +
          '     dcterms:title ?label ; ' +
          '     a void:Dataset ; ' +
          '     es:head ?headVersion . ' +
          '   OPTIONAL { ?datasetUri dcterms:description ?comment . } ' +
          ' } }';
        return DatasetService.executeQuery(scratchUri, query);
      });
    }

    function reset() {
      loadDefer = $q.defer();
    }

    return {
      loadStore: loadStore,
      queryDatasetsOverview: queryDatasetsOverview,
      reset:reset
    };
  };

  return dependencies.concat(DatasetOverviewService);
});
