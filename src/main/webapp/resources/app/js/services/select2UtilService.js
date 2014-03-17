'use strict';
define(['angular', 'underscore'], function () {
  var dependencies = [];
  var Select2UtilService = function () {
    return {
      idsToObjects: function (selectedIds, objects) {
        return _.map(selectedIds, function (id) {
          return _.find(objects, function (object) {
            return object && object.id === parseInt(id, 10);
          });
        });
      },
      objectsToIds: function (objects) {
        return _.map(objects, function (object) {
          return object.id.toString();
        });
      }
    };
  };
  return dependencies.concat(Select2UtilService);
});