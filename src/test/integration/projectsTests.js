var login = require('./util/login.js');
var ProjectsPage = require('./pages/projects.js');

module.exports = {
  "Addis create project test" : function (browser) {
    login(browser, 'https://addis-test.drugis.org');
    projects = new ProjectsPage(browser);
    projects.createProject();
    projects.end();
  }
};