'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var StudyResource = function($resource) {
    return $resource(
      '/datasets/:datasetUUID/studies/:studyUUID', {
        datasetUUID: '@datasetUUID',
        studyUUID: '@studyUUID'
      }, {
        'get': {
          method: 'get',
          headers: {
            'Content-Type': 'text/n3'
          },
          transformResponse: function(data) {
            return {
              data: data // property on Responce object to access raw result data
            };
          }
        },
        'put': {
          method: 'put'
        }
      });
  };
  return dependencies.concat(StudyResource);
});
