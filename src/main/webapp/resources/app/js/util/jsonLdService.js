'use strict';
define(['angular'], function() {
  var dependencies = [];

  function JsonLdService() {

    /*
    * takes a array of jsonLd object and looks for '@id' properties, 
    * if an object has a '@id' property is takes it value and stripsof the preable and places the rest on a new uuid property
    *
    * example [{@id:'namespace:asd-213-asd'}] becomes [{@id:'namespace:asd-213-asd', uuuid:asd-213-asd}]
    */
    function rewriteAtIds(jsonLdObjectArray) {
      var copy = [];
      _.each(jsonLdObjectArray, function(object) {
        if (object['@id']) {
          object.uuid = object['@id'].split(':').pop();
          copy.push(object);
        }
      });
      return copy;
    }

    return {
      rewriteAtIds: rewriteAtIds
    };
  }
  return dependencies.concat(JsonLdService);
});