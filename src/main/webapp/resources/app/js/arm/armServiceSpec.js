'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the arm service', function() {

    var mockStudyService = jasmine.createSpyObj('StudyService', ['doQuery']);

    beforeEach(module('trialverse.arm'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', mockStudyService);
      });
    });


    describe('edit', function() {
      it('should do a query with replaced values', inject(function($rootScope, $httpBackend, ArmService) {

        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/app/sparql/editArmWithComment.sparql', false);
        xmlHTTP.send(null);
        console.log(xmlHTTP.responseText);

        xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.txt', false);
        xmlHTTP.send(null);
        console.log(xmlHTTP.responseText);

        $httpBackend.expectGET('app/sparql/editArmWithComment.sparql').respond(xmlHTTP.responseText);

        var mockArm = {
          armURI: {
            value: 'armURIValue'
          },
          label: {
            value: 'armLabelValue'
          },
          comment: {
            value: 'armCommentValue'
          }
        };

        $httpBackend.flush();
        $rootScope.$digest();

        ArmService.edit(mockArm);

        $rootScope.$digest();

        var queryWithSubstitutions = 'foo';

        expect(mockStudyService.doQuery).toHaveBeenCalledWith(queryWithSubstitutions);

      }));
    });
  });
});
