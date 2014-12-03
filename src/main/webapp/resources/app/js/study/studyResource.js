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
              n3Data: data // property on Responce object to acces raw result data 
            };
          }
        }
      }, {
        'put': {
          method: 'put'
        }
      });
  };
  return dependencies.concat(StudyResource);
});