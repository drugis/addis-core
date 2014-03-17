define(['angular', 'angular-mocks', 'services'], function () {
  describe("The select2 utility  service", function () {
    var ids = [1, 2],
      objects = [{
        id: 1
      }, {
        id: 3
      }, {
        id: 2
      }],
      expectedObjects = [{
        id: 1
      }, {
        id: 2
      }],
      expectedIds = ['1', '3', '2'];

    beforeEach(module('addis.services'));

    beforeEach(inject(function (Select2UtilService) {
      this.select2UtilService = Select2UtilService;
    }));

    it('should convert a list of ids to a list of objects with those ids', function () {
      var result = this.select2UtilService.idsToObjects(ids, objects);
      expect(result).toEqual(expectedObjects);
    });

    it('should convert a list of objects to a list of strings containing their ids', function () {
      var result = this.select2UtilService.objectsToIds(objects);
      expect(result).toEqual(expectedIds);
    })
  });
});