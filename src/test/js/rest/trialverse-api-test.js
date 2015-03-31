var should = require('should');
var assert = require('assert');
var request = require('supertest');
var fs = require('fs');
var path = require('path');

var trialverseUrl = process.env.TRIALVERSE_URL;
var sessionId = 'CA2177C090923A7195FB3BDF7CB9B2D8';
var csrfToken = '9f410b8f-ccf1-4404-9eae-4720e3ffc748';

var newDataset = '{"title":"my-test-dataset","description":"my test  description"}';
var newStudy = '<http://trials.drugis.org/graphs/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
  ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
  ' a  <http://trials.drugis.org/ontology#Study> ; ' +
  ' <http://trials.drugis.org/ontology#has_epochs> () . ';
var queryStudyWithDetails = fs.readFileSync(path.join(__dirname, '../../../main/webapp/resources/app/sparql/queryStudiesWithDetails.sparql'), 'utf8');


function createDataset(callback) {
  request(trialverseUrl)
    .post('/datasets')
    .set('Content-Type', 'application/json;charset=UTF-8')
    .set('Cookie', 'JSESSIONID=' + sessionId)
    .set('X-CSRF-TOKEN', csrfToken)
    .send(newDataset)
    .end(function(err, res) {
      // console.log(JSON.stringify(res));
      var datasetUrl = res.headers.location;
      callback(datasetUrl);
    });
}

function createStudy(datasetUuid, callback) {
  request(trialverseUrl + '/datasets/' + datasetUuid)
    .put('/graphs/studyUuid?commitTitle="mocha test create study"')
    .set('Content-Type', 'text/turtle')
    .set('Cookie', 'JSESSIONID=' + sessionId)
    .set('X-CSRF-TOKEN', csrfToken)
    .send(newStudy)
    .end(function() {
      callback(datasetUuid);
    });
}

function createDatasetAndStudy(callback) {
  createDataset(function(datasetUrl) {
    var datasetUuid = datasetUrl.split('/')[4];
    console.log('dataset created url = ' + datasetUrl);
    createStudy(datasetUuid, callback);
  });
}

describe('create dataset ', function() {

  it('should return a 201 ( created )', function(done) {
    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        if (err) {
          //console.log('err =  ' + err);
          //throw err;
        }
        // console.log('res = ' + JSON.stringify(res));
        res.should.have.property('status', 201);
        done();
      });
  });
});

describe('get dataset', function() {
  var datasetUrl, datasetUuid;

  before(function(done) {
    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        datasetUrl = res.headers.location;
        datasetUuid = datasetUrl.split('/')[4];
        done();
      });
  });

  it('should get the dataset', function(done) {
    var expectedTtl =
      '<' + datasetUrl + '>\n' +
      '        a       <http://rdfs.org/ns/void#Dataset> ;\n' +
      '        <http://purl.org/dc/terms/description>\n' +
      '                "my test  description" ;\n' +
      '        <http://purl.org/dc/terms/title>\n' +
      '                "my-test-dataset" .\n';

    request(trialverseUrl)
      .get(/datasets/ + datasetUuid)
      .set('Accept', 'text/turtle')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .end(function(err, res) {
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });
});


///// NB DOES NOT WORK DUE TO BROKEN PIPE !?!?!

describe('query datasets', function() {

  var _datasetUuid;

  before(function(done) {

    createDataset(function() {
      createDataset(function() {
        createDataset(function() {
          done();
        });
      });
    });
  });

  it('should query the data sets', function(done) {
    var expectedTtl = 'bool shit';

    request(trialverseUrl)
      .get('/datasets')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .set('Accept', 'text/turtle')
      .end(function(err, res) {
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.length.should.be.above(0);
        done();
      });
  });

});

describe.skip('get study', function() {

  var _datasetUuid;

  before(function(done) {
    createDatasetAndStudy(function(datasetUuid) {
      console.log('dataset uuid extracted ' + datasetUuid);
      _datasetUuid = datasetUuid;
      done();
    });
  });

  it('should get the study', function(done) {
    var expectedTtl = '<http://trials.drugis.org/graphs/studyUuid>\n' +
      '        a       <http://trials.drugis.org/ontology#Study> ;\n' +
      '        <http://www.w3.org/2000/01/rdf-schema#comment>\n' +
      '                "myComment" ;\n' +
      '        <http://www.w3.org/2000/01/rdf-schema#label>\n' +
      '                "mystudy" ;\n' +
      '        <http://trials.drugis.org/ontology#has_epochs>\n' +
      '                ()\n' +
      ' .\n';

    request(trialverseUrl)
      .get('/datasets/' + _datasetUuid + '/graphs/studyUuid')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .set('Accept', 'text/turtle')
      .end(function(err, res) {
        //console.log('++++Get study responcay+++++++\n\n\n' + JSON.stringify(res));
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });
});

describe.only('query studies with detail', function() {

  var _datasetUrl, _datasetUuid;

  before(function(done) {
    createDataset(function(datasetUrl) {
      _datasetUrl = datasetUrl;
      _datasetUuid = datasetUrl.split('/')[4];

      done();
    });
  });

  it('should work when there are no studies', function(done) {
    request(trialverseUrl)
      .get('/datasets/' + _datasetUuid + '/query?query=' + encodeURIComponent(queryStudyWithDetails))
      .set('Content-Type', 'application/sparql-query')
      .set('Accept', 'application/sparql-results+json')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .end(function(err, res) {
        res.should.have.property('status', 200);
        JSON.parse(res.text).results.bindings.length.should.equal(0);
        done();
      });
  });

});

describe.only('query studies with detail', function() {

  var  _datasetUuid;

  before(function(done) {
    createDatasetAndStudy(function(datasetUuid) {
      _datasetUuid = datasetUuid;
      done();
    });
  });

  it('should work when there is a study', function(done) {
    request(trialverseUrl)
      .get('/datasets/' + _datasetUuid + '/query?query=' + encodeURIComponent(queryStudyWithDetails))
      .set('Content-Type', 'application/sparql-query')
      .set('Accept', 'application/sparql-results+json')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .end(function(err, res) {
        res.should.have.property('status', 200);
        JSON.parse(res.text).results.bindings.length.should.equal(1);
        done();
      });
  });

});



//
// node-debug -p 8030 _mocha jena-api-test.js
//
