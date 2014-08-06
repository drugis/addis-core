define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The namespaceController", function() {
      var scope, ctrl, trialverseStudiesWithDetailsResource, trialverseResource, mockStateParams;

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller) {
        mockStateParams = {
          namespaceUid: 'asd-123'
        };
    
        trialverseStudiesWithDetailsResource = jasmine.createSpyObj('trialverseStudiesWithDetailsResource', ['get']);
        trialverseStudiesWithDetailsResource.get.and.returnValue([{study:{name:'name1'}},{study: {name:'name2'}}]);

        trialverseResource = jasmine.createSpyObj('trialverseResource', ['get']);
        trialverseResource.get.and.returnValue({
          name: 'namespace name'
        });

        scope = {};

        ctrl = $controller('NamespaceController', {
          $scope: scope,
          $stateParams: mockStateParams,
          'TrialverseResource': trialverseResource,
          'TrialverseStudiesWithDetailsResource': trialverseStudiesWithDetailsResource
        });
      }));

      describe("initialisation", function() {

        it("should make the namespace details available on the scope", function() {
          expect(scope.namespace.name).toBe('namespace name');
        });

        it("should make a list of studyDetails available on the scope", function() {
          expect(scope.studiesWithDetails[0].study.name).toBe('name1');
        });
      });


    });
  });