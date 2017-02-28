define('app',['exports', './web-api'], function (exports, _webApi) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.App = undefined;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var App = exports.App = function () {
    App.inject = function inject() {
      return [_webApi.WebAPI];
    };

    function App(api) {
      _classCallCheck(this, App);

      this.api = api;
    }

    App.prototype.configureRouter = function configureRouter(config, router) {
      config.title = 'Candidate Repository';

      config.map([{ route: '', moduleId: 'no-selection', title: 'home' }, { route: 'candidates/new', moduleId: 'candidate-form', name: 'candidateSubmission' }, { route: 'candidates/:id', moduleId: 'candidate-details', name: 'candidates' }]);

      this.router = router;
    };

    return App;
  }();
});
define('candidate-details',['exports', 'aurelia-event-aggregator', './web-api', './messages', './utility'], function (exports, _aureliaEventAggregator, _webApi, _messages, _utility) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.CandidateDetail = undefined;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var CandidateDetail = exports.CandidateDetail = function () {
    CandidateDetail.inject = function inject() {
      return [_webApi.WebAPI, _aureliaEventAggregator.EventAggregator];
    };

    function CandidateDetail(api, ea) {
      _classCallCheck(this, CandidateDetail);

      this.api = api;
      this.ea = ea;
    }

    CandidateDetail.prototype.activate = function activate(params, routeConfig) {
      var _this = this;

      this.routeConfig = routeConfig;

      return this.api.getCandidateDetails(params.id).then(function (candidate) {
        _this.candidate = candidate;
        _this.routeConfig.navModel.setTitle(candidate.name);
        _this.originalCandidate = JSON.parse(JSON.stringify(candidate));
        _this.ea.publish(new _messages.CandidateViewed(_this.candidate));
      });
    };

    return CandidateDetail;
  }();
});
define('candidate-form',['exports', 'aurelia-event-aggregator', './web-api', './messages', './utility', 'aurelia-router'], function (exports, _aureliaEventAggregator, _webApi, _messages, _utility, _aureliaRouter) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.CandidateForm = undefined;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var _createClass = function () {
    function defineProperties(target, props) {
      for (var i = 0; i < props.length; i++) {
        var descriptor = props[i];
        descriptor.enumerable = descriptor.enumerable || false;
        descriptor.configurable = true;
        if ("value" in descriptor) descriptor.writable = true;
        Object.defineProperty(target, descriptor.key, descriptor);
      }
    }

    return function (Constructor, protoProps, staticProps) {
      if (protoProps) defineProperties(Constructor.prototype, protoProps);
      if (staticProps) defineProperties(Constructor, staticProps);
      return Constructor;
    };
  }();

  var CandidateForm = exports.CandidateForm = function () {
    CandidateForm.inject = function inject() {
      return [_webApi.WebAPI, _aureliaEventAggregator.EventAggregator, _aureliaRouter.Router];
    };

    function CandidateForm(api, ea, router) {
      _classCallCheck(this, CandidateForm);

      this.candidate = {
        fullname: null,
        email: null,
        experience: null,
        recruiterEmail: null,
        skills: null
      };

      this.api = api;
      this.ea = ea;
      this.appRouter = router;
    }

    CandidateForm.prototype.submit = function submit() {
      var _this = this;

      var candidateData = {
        fullname: this.candidate.fullname,
        email: this.candidate.email,
        experience: Number(this.candidate.experience),
        recruiterEmail: this.candidate.recruiterEmail,
        skills: this.candidate.skills
      };
      console.log('candidate', candidateData);
      this.api.submitCandidate(candidateData).then(function (res) {
        console.log('Receieved response from API', JSON.stringify(res));
        _this.appRouter.navigate('');
      }).catch(function (err) {
        console.log('errror', err);
      });
    };

    _createClass(CandidateForm, [{
      key: 'canSave',
      get: function get() {
        return this.candidate.fullname && this.candidate.email && this.candidate.experience && this.candidate.recruiterEmail && this.candidate.skills && !this.api.isRequesting;
      }
    }]);

    return CandidateForm;
  }();
});
define('candidate-list',['exports', 'aurelia-event-aggregator', './web-api', './messages'], function (exports, _aureliaEventAggregator, _webApi, _messages) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.CandidateList = undefined;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var _class, _temp;

  var CandidateList = exports.CandidateList = (_temp = _class = function () {
    function CandidateList(api, ea) {
      var _this = this;

      _classCallCheck(this, CandidateList);

      console.log('inside CandidateList');
      this.api = api;
      this.candidates = [];

      ea.subscribe(_messages.CandidateViewed, function (msg) {
        return _this.select(msg.candidate);
      });
      ea.subscribe(_messages.CandidateUpdated, function (msg) {
        var id = msg.candidate.id;
        var found = _this.candidates.find(function (x) {
          return x.id === id;
        });
        Object.assign(found, msg.candidate);
      });
    }

    CandidateList.prototype.created = function created() {
      var _this2 = this;

      console.log('fetching CandidateList');
      this.api.getCandidateList().then(function (candidates) {
        _this2.candidates = candidates;
      });
    };

    CandidateList.prototype.select = function select(candidate) {
      this.selectedId = candidate.id;
      return true;
    };

    return CandidateList;
  }(), _class.inject = [_webApi.WebAPI, _aureliaEventAggregator.EventAggregator], _temp);
});
define('environment',["exports"], function (exports) {
  "use strict";

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.default = {
    debug: true,
    testing: true
  };
});
define('main',['exports', './environment'], function (exports, _environment) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.configure = configure;

  var _environment2 = _interopRequireDefault(_environment);

  function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : {
      default: obj
    };
  }

  Promise.config({
    warnings: {
      wForgottenReturn: false
    }
  });

  function configure(aurelia) {
    aurelia.use.standardConfiguration().feature('resources');

    if (_environment2.default.debug) {
      aurelia.use.developmentLogging();
    }

    if (_environment2.default.testing) {
      aurelia.use.plugin('aurelia-testing');
    }

    aurelia.start().then(function () {
      return aurelia.setRoot();
    });
  }
});
define('messages',["exports"], function (exports) {
  "use strict";

  Object.defineProperty(exports, "__esModule", {
    value: true
  });

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var CandidateUpdated = exports.CandidateUpdated = function CandidateUpdated(candidate) {
    _classCallCheck(this, CandidateUpdated);

    this.candidate = candidate;
  };

  var CandidateViewed = exports.CandidateViewed = function CandidateViewed(candidate) {
    _classCallCheck(this, CandidateViewed);

    this.candidate = candidate;
  };

  var CandidateSubmitted = exports.CandidateSubmitted = function CandidateSubmitted(candidate) {
    _classCallCheck(this, CandidateSubmitted);

    this.candidate = candidate;
  };
});
define('no-selection',["exports"], function (exports) {
  "use strict";

  Object.defineProperty(exports, "__esModule", {
    value: true
  });

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var NoSelection = exports.NoSelection = function NoSelection() {
    _classCallCheck(this, NoSelection);

    this.message = "Please Select a Contact.";
  };
});
define('utility',["exports"], function (exports) {
	"use strict";

	Object.defineProperty(exports, "__esModule", {
		value: true
	});
	exports.areEqual = areEqual;
	function areEqual(obj1, obj2) {
		return Object.keys(obj1).every(function (key) {
			return obj2.hasOwnProperty(key) && obj1[key] === obj2[key];
		});
	};
});
define('web-api',['exports', 'axios'], function (exports, _axios) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.WebAPI = undefined;

  var _axios2 = _interopRequireDefault(_axios);

  function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : {
      default: obj
    };
  }

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var axiosInstance = _axios2.default.create({
    baseURL: 'https://9own2oqyrh.execute-api.us-east-1.amazonaws.com/dev/candidates'
  });

  var WebAPI = exports.WebAPI = function () {
    function WebAPI() {
      _classCallCheck(this, WebAPI);

      this.isRequesting = false;
    }

    WebAPI.prototype.getCandidateList = function getCandidateList() {
      var _this = this;

      this.isRequesting = true;
      return axiosInstance.get('').then(function (response) {
        _this.isRequesting = false;
        var data = response.data;
        return data.candidates;
      }).catch(function (err) {
        console.log("Received failure from API", err);
        _this.isRequesting = false;
        throw err;
      });
    };

    WebAPI.prototype.getCandidateDetails = function getCandidateDetails(candidateId) {
      var _this2 = this;

      this.isRequesting = true;
      return axiosInstance.get('/' + candidateId).then(function (response) {
        _this2.isRequesting = false;
        return response.data;
      }).catch(function (err) {
        console.log("Received failure from API", err);
        _this2.isRequesting = false;
        throw err;
      });
    };

    WebAPI.prototype.submitCandidate = function submitCandidate(candidate) {
      var _this3 = this;

      this.isRequesting = true;
      return axiosInstance.post('', candidate).then(function (res) {
        _this3.isRequesting = false;
        return res;
      }).catch(function (err) {
        console.log("Received failure from API", err);
        _this3.isRequesting = false;
        throw err;
      });;
    };

    return WebAPI;
  }();
});
define('resources/index',['exports'], function (exports) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.configure = configure;
  function configure(config) {
    config.globalResources(['./elements/loading-indicator']);
  }
});
define('resources/elements/loading-indicator',['exports', 'nprogress', 'aurelia-framework'], function (exports, _nprogress, _aureliaFramework) {
  'use strict';

  Object.defineProperty(exports, "__esModule", {
    value: true
  });
  exports.LoadingIndicator = undefined;

  var nprogress = _interopRequireWildcard(_nprogress);

  function _interopRequireWildcard(obj) {
    if (obj && obj.__esModule) {
      return obj;
    } else {
      var newObj = {};

      if (obj != null) {
        for (var key in obj) {
          if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key];
        }
      }

      newObj.default = obj;
      return newObj;
    }
  }

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  var LoadingIndicator = exports.LoadingIndicator = (0, _aureliaFramework.decorators)((0, _aureliaFramework.noView)(['nprogress/nprogress.css']), (0, _aureliaFramework.bindable)({ name: 'loading', defaultValue: false })).on(function () {
    function _class() {
      _classCallCheck(this, _class);
    }

    _class.prototype.loadingChanged = function loadingChanged(newValue) {
      if (newValue) {
        nprogress.start();
      } else {
        nprogress.done();
      }
    };

    return _class;
  }());
});
define('text!app.html', ['module'], function(module) { module.exports = "<template><require from=\"bootstrap/css/bootstrap.css\"></require><require from=\"./styles.css\"></require><require from=\"./candidate-list\"></require><nav class=\"navbar navbar-default navbar-fixed-top\" role=\"navigation\"><div class=\"container-fluid\"><div class=\"navbar-header\"><a class=\"navbar-brand\" href=\"#\"><i class=\"fa fa-user\"></i> <span>Candidates</span></a></div><a route-href=\"route: candidateSubmission;\" class=\"btn btn-success navbar-btn pull-right\">Submit Candidate</a></div></nav><loading-indicator loading.bind=\"router.isNavigating || api.isRequesting\"></loading-indicator><div class=\"container\"><div class=\"row\"><candidate-list class=\"col-md-4\"></candidate-list><router-view class=\"col-md-8\"></router-view></div></div></template>"; });
define('text!styles.css', ['module'], function(module) { module.exports = "body { padding-top: 70px; }\n\nsection {\n  margin: 0 20px;\n}\n\na:focus {\n  outline: none;\n}\n\n.navbar-nav li.loader {\n    margin: 12px 24px 0 6px;\n}\n\n.no-selection {\n  margin: 20px;\n}\n\n.contact-list {\n  overflow-y: auto;\n  border: 1px solid #ddd;\n  padding: 10px;\n}\n\n.panel {\n  margin: 20px;\n}\n\n.button-bar {\n  right: 0;\n  left: 0;\n  bottom: 0;\n  border-top: 1px solid #ddd;\n  background: white;\n}\n\n.button-bar > button {\n  float: right;\n  margin: 20px;\n}\n\nli.list-group-item {\n  list-style: none;\n}\n\nli.list-group-item > a {\n  text-decoration: none;\n}\n\nli.list-group-item.active > a {\n  color: white;\n}\n"; });
define('text!candidate-details.html', ['module'], function(module) { module.exports = "<template><div class=\"panel panel-primary\"><div class=\"panel-heading\"><h3 class=\"panel-title\">Submit Candidate</h3></div><div class=\"panel-body\"><form role=\"form\" class=\"form-horizontal\"><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Full Name</label><div class=\"col-sm-10\"><input type=\"text\" placeholder=\"Full name of candidate\" class=\"form-control\" value.bind=\"candidate.fullname\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Email</label><div class=\"col-sm-10\"><input type=\"email\" placeholder=\"A valid email of candidate\" class=\"form-control\" value.bind=\"candidate.email\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Experience</label><div class=\"col-sm-10\"><input type=\"number\" placeholder=\"Experience in years\" class=\"form-control\" value.bind=\"candidate.experience\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Skills</label><div class=\"col-sm-10\"><input type=\"email\" placeholder=\"Candidate skills\" class=\"form-control\" value.bind=\"candidate.skills\"></div></div></form></div></div><div class=\"button-bar\"><button class=\"btn btn-success\" click.delegate=\"save()\" disabled.bind=\"!canSave\">Save</button></div></template>"; });
define('text!candidate-form.html', ['module'], function(module) { module.exports = "<template><div class=\"panel panel-primary\"><div class=\"panel-heading\"><h3 class=\"panel-title\">Submit Candidate</h3></div><div class=\"panel-body\"><form role=\"form\" class=\"form-horizontal\"><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Full Name</label><div class=\"col-sm-10\"><input type=\"text\" placeholder=\"Full name of candidate\" class=\"form-control\" value.bind=\"candidate.fullname\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Email</label><div class=\"col-sm-10\"><input type=\"email\" placeholder=\"A valid email of candidate\" class=\"form-control\" value.bind=\"candidate.email\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Experience</label><div class=\"col-sm-10\"><input type=\"number\" placeholder=\"Experience in years\" class=\"form-control\" value.bind=\"candidate.experience\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Recruiter Email</label><div class=\"col-sm-10\"><input type=\"email\" placeholder=\"Email of recruiter who is submitting the candidate details\" class=\"form-control\" value.bind=\"candidate.recruiterEmail\"></div></div><div class=\"form-group\"><label class=\"col-sm-2 control-label\">Skills</label><div class=\"col-sm-10\"><input type=\"email\" placeholder=\"Candidate skills\" class=\"form-control\" value.bind=\"candidate.skills\"></div></div></form></div></div><div class=\"button-bar\"><button class=\"btn btn-success\" click.delegate=\"submit()\" disabled.bind=\"!canSave\">Save</button></div></template>"; });
define('text!candidate-list.html', ['module'], function(module) { module.exports = "<template><div class=\"candidate-list\"><ul class=\"list-group\"><li repeat.for=\"candidate of candidates\" class=\"list-group-item ${candidate.id === $parent.selectedId ? 'active' : ''}\"><a route-href=\"route: candidates; params.bind: {id:candidate.id}\" click.delegate=\"$parent.select(candidate)\"><h4 class=\"list-group-item-heading\">${candidate.name}</h4><p class=\"list-group-item-text\">${candidate.email}</p></a></li></ul></div></template>"; });
define('text!no-selection.html', ['module'], function(module) { module.exports = "<template><div class=\"no-selection text-center\"><h2>${message}</h2></div></template>"; });
//# sourceMappingURL=app-bundle.js.map