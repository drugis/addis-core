'use strict';
define([], function() {

  var dependencies = ['$resource'];

  var StudyResource = function($resource) {

    return $resource('/datasets/:datasetUUID/studies/:studyUUID', {
      datasetUUID: '@datasetUUID',
      studyUUID: '@studyUUID'
    }, {
      'put': {
        method: 'put'
      }
    });
  };
  return dependencies.concat(StudyResource);
});
