'use strict';
define(['angular'], function() {
  var dependencies = [];

  function UUIDService() {
    var graphPrefix = 'http://trials.drugis.org/graphs/';
    var datasetPrefix = 'http://trials.drugis.org/datasets/';

    // see: http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
    // This means a block-formatted string like \'abcdefgh-ijkl-4mno-$pqr-stuvwxyz1234\'
    // with a 4 at the first position of the third block (indicating it's random)
    // and $ is one of 8, 9, a, or b
    function generate() {
      var pattern = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
      return pattern.replace(/[xy]/g, function(c) {
        var
          r = Math.random() * 16 | 0,
          v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    }
    function generateGraphUri() {
      return graphPrefix + generate();
    }
    function buildGraphUri(datasetUuid, versionUuid, graphUuid) {
      return datasetPrefix + datasetUuid + '/versions/' + versionUuid + '/graphs/' + graphUuid;
    }
    function buildDatasetUri(uuid) {
      return datasetPrefix + uuid;
    }
    function getUuidFromNamespaceUrl(url) {
      return url.split('/')[4];
    }
    return {
      generate: generate,
      generateGraphUri: generateGraphUri,
      buildDatasetUri: buildDatasetUri,
      buildGraphUri: buildGraphUri,
      getUuidFromNamespaceUrl: getUuidFromNamespaceUrl
    };
  }
  return dependencies.concat(UUIDService);
});
