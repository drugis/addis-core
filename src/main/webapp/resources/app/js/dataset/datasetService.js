'use strict';
var DatasetService = define(['angular'], function() {
  var dependencies = [];

  var DatasetService = function() {
    function createDataset(dataset, callback) {
      callback(dataset);
    }
    return {
      createDataset: createDataset
    };
  };

  return dependencies.concat(DatasetService);
});