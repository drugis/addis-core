'use strict';
define([],
  function() {
    var dependencies = ['$resource', 'StudyService'];
    var ArmService = function($resource, StudyService) {

      var query ;
      getResource('editArmWithComment.sparql');


      function getResource(name) {
        return $resource('app/js/study/directives/arm/sparql/:name', {name: name}, {
          'get': {
            method: 'get',
            transformResponse: function(data) {
              return {
                data: data 
              };
            }
          }
        }).get(function(result) {
          query = result;
        });
      }

      function edit(arm) {
        var editArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
                              .replace('$newArmLabel', arm.label.value)
                              .replace('$newArmComment', arm.comment.value);

        return StudyService.doQuery(editArmQuery);
      }


      return {
        edit: edit
      };
    };

    return dependencies.concat(ArmService);
  });