'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the adverse event service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var measurementMomentServiceMock;
    var adverseEventService;
    var studyService;

    var addTemplateRaw
    var addAdverseEventQueryRaw;
    var adverseEventsQuery;
    var deleteAdverseEventRaw;
    var editAdverseEventRaw;
    var queryAdverseEventMeasuredAtRaw;

    // private helper methods

    function queryTeststore(query) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open("POST", scratchStudyUri + '/query?output=json', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-query');
      xmlHTTP.setRequestHeader('Accept', 'application/ld+json');
      xmlHTTP.send(query);
      var result = xmlHTTP.responseText;
      console.log('queryTeststore result = ' + result);
      return result;
    }

    function dropGraph(uri) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open("POST", scratchStudyUri + '/update', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
      xmlHTTP.send('DROP GRAPH <' + uri + '>');
      return true;
    }

    function deFusekify(data) {
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.object(_.map(_.pairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    function loadTemplate(templateName) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('GET', 'base/app/sparql/' + templateName, false);
      xmlHTTP.send(null);
      var template = xmlHTTP.responseText;
      httpBackend.expectGET('app/sparql/' + templateName).respond(template);
      return template;
    }

    function executeUpdateQuery(query) {
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open("POST", scratchStudyUri + '/update', false);
      xmlHTTP.setRequestHeader('Content-type', 'application/sparql-update');
      xmlHTTP.send(query);
      return xmlHTTP.responseText;
    }

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = jasmine.createSpyObj('RemoteRdfStoreService', [
          'create',
          'load',
          'executeUpdate',
          'executeQuery',
          'getGraph',
          'deFusekify'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.adverseEvent'));

    beforeEach(function() {
      module('trialverse', function($provide) {

        measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', [
          'queryItems',
          'addItem',
          'editItem',
          'deleteIte ',
          'generateLabel'
        ]);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);

      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, AdverseEventService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      adverseEventService = AdverseEventService;
      studyService = StudyService;

      // reset the test graph
      dropGraph(graphUri);

      // load service templates and flush httpBackend
      addTemplateRaw = loadTemplate('addTemplate.sparql');
      addAdverseEventQueryRaw = loadTemplate('addAdverseEvent.sparql');
      adverseEventsQuery = loadTemplate('queryAdverseEvent.sparql');
      deleteAdverseEventRaw = loadTemplate('deleteAdverseEvent.sparql');
      editAdverseEventRaw = loadTemplate('editAdverseEvent.sparql');
      queryAdverseEventMeasuredAtRaw = loadTemplate('queryMeasuredAt.sparql');
      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      var createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      var loadStoreDeferred = $q.defer();
      var loadStorePromise = loadStoreDeferred.promise;
      remotestoreServiceStub.load.and.returnValue(loadStorePromise);

      var loadStorePromise = studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();
      rootScope.$digest();

      // stub remotestoreServiceStub.executeUpdate method
      remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
        query = query.replace(/\$graphUri/g, graphUri);

        console.log('graphUri = ' + uri);
        console.log('query = ' + query);

        executeUpdateQuery(query);

        var executeUpdateDeferred = q.defer();
        executeUpdateDeferred.resolve();
        return executeUpdateDeferred.promise;
      });

    }));

    describe('addItem', function() {

      it('should add the adverseEvent triples and measuredAtMoments triples to the graph', function(done) {

        // the test item to add 
        var adverseEvent = {
          label: 'adverse event label',
          measurementType: 'http://some-measurementType'
        };

        // add some measured at moments 
        var moment1 = {uri: 'http://moments/moment1'};
        var moment2 = {uri: 'http://moments/moment2'};
        var adverseEventWithMoments = angular.copy(adverseEvent);
        adverseEventWithMoments.measuredAtMoments = [moment1, moment2];

        // call the method to test
        var resultPromise = adverseEventService.addItem(adverseEventWithMoments);

        // setup verification, ready for digest cycle to kickoff 
        resultPromise.then(function(result) {
          // verify addAdverseEvent query
          var adverseEventsAsString = queryTeststore(adverseEventsQuery.replace(/\$graphUri/g, graphUri));
          var adverseEventsObject = deFusekify(adverseEventsAsString);
          expect(adverseEventsObject.length).toEqual(1);
          expect(adverseEventsObject[0].label).toEqual(adverseEvent.label);
          expect(adverseEventsObject[0].measurementType).toEqual(adverseEvent.measurementType);

          // verify add measured at query
          var measuredAtQuery = queryAdverseEventMeasuredAtRaw.replace(/\$graphUri/g, graphUri);
          var adverseEventMeasuredAtAsString = queryTeststore(measuredAtQuery);
          var measuredAtMoments = deFusekify(adverseEventMeasuredAtAsString);
          expect(measuredAtMoments.length).toEqual(2);
          expect(measuredAtMoments[0].measurementMoment).toEqual('http://moments/moment2');
          expect(measuredAtMoments[1].measurementMoment).toEqual('http://moments/moment1');

          // do not forget to signal async test is done !
          done();
        });

        // fire in the hole !
        rootScope.$digest();

      });
    });


  });
});