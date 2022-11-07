define(['jquery', 'bootbox'], function($, bootbox){

	/**
	 * @author stephen.young@zumba.com
	 * @constructor
	 */
	var Popper = function(){};

	/**
	 * Flat map of semantic interface methods to Deferred object methods.
	 *
	 * @type Array [interfaceA, deferredA [, ...]]
	 */
	Popper.methodMap = [
		'ok',         'done',
		'ajaxDone',   'done',
		'cancel',     'fail',
		'ajaxFail',   'fail',
		'ajaxAlways', 'always'
	];

	/**
	 * Shorthand for 'ok' interface options
	 *
	 * @type Array
	 */
	var ok = ['ok'];

	/**
	 * Shorthand for 'ok' and 'cancel' interface options
	 *
	 * @type Array
	 */
	var okCancel = ok.concat('cancel');

	/**
	 * Shorthand for 'done' and 'fail' and 'always' interface options
	 *
	 * @type Array
	 */
	var ajaxMap = ['ajaxDone', 'ajaxFail', 'ajaxAlways'];

	/**
	 * Shorthand for $.Deferred constructor
	 * (reduces $.property lookups)
	 *
	 * @constructor
	 */
	var Deferred = $.Deferred;

	/**
	 * Pop an alert modal (info)
	 *
	 * @access public
	 * @param String msg
	 * @return Object interface
	 */
	Popper.prototype.alert = function(msg){
		var d = defer();
		bootbox.alert(msg, d.resolve);
		return getInterface(d, ok);
	};

	/**
	 * Pop a confirmation modal (boolean)
	 *
	 * response.ok() for confirmation,
	 * response.cancel() for cancel
	 *
	 * @access public
	 * @param String msg
	 * @return Object interface
	 */
	Popper.prototype.confirm = function(msg){
		var d = defer();
		bootbox.confirm(msg, function(confirmed){
			d[confirmed ? 'resolve' : 'reject']();
		});
		return getInterface(d, okCancel);
	};

	/**
	 * Pop a confirmation modal (boolean) and perform an ajax request if confirmed.
	 *
	 * response.ok() for confirmation, not async
	 * response.cancel() for cancel
	 * response.done() for ajax done, async
	 * response.fail() for ajax fail, async
	 * response.always() for ajax always, async
	 *
	 * @access public
	 * @param String msg
	 * @param Object ajax options
	 * @return Object interface
	 */
	Popper.prototype.confirmAjax = function(msg, options){
		var response = defer();
		var popup = this.confirm(msg);

		// map the ajax methods to a new deferred, and merge it with popup
		$.extend(popup, getInterface(response, ajaxMap));

		return popup.ok(function(){
			// when ajax resolves, pipe results to response deferred
			$.ajax(options)
				.done(response.resolve)
				.fail(response.reject);
		});
	};

	/**
	 * Pop a confirmation modal (boolean) and perform an ajax DELETE request if confirmed.
	 *
	 * Standard "This is not recoverable" message is added here.
	 *
	 * Additional ajax options can be passed as a third param, if needed.
	 *
	 * response.ok() for confirmation, not async
	 * response.cancel() for cancel
	 * response.done() for ajax done, async
	 * response.fail() for ajax fail, async
	 * response.always() for ajax always, async
	 *
	 * @access public
	 * @param String msg
	 * @param String url
	 * @param Object ajax options
	 * @return Object interface
	 */
	Popper.prototype.confirmDelete = function(msg, url, options) {
		return this.confirmAjax(msg, $.extend({}, { url : url, type : 'DELETE' }, options));
	};

	/**
	 * Prompt the user with a text input modal
	 *
	 * response.ok() for confirmation,
	 * response.cancel() for cancel
	 *
	 * @access public
	 * @param String msg
	 * @return Object interface
	 */
	Popper.prototype.prompt = function(msg){
		var d = defer();
		bootbox.prompt(msg, function(text){
			d[text ? 'resolve' : 'reject'](text);
		});
		return getInterface(d, okCancel);
	};

	/**
	 * Get a deferred object
	 *
	 * @access private
	 * @param void
	 * @return jQuery.Deferred
	 */
	var defer = function(){ return new Deferred(); };

	/**
	 * Get an interface for the dialog responses that maps to the deferred object methods
	 *
	 * @access private
	 * @param jQuery.Deferred d
	 * @param Array methods
	 * @return Object { ok : done, cancel : fail }
	 */
	var getInterface = function(d, methods) {
		var map = Popper.methodMap;
		var obj = {};

		// map methods to their deferred counterparts
		$.each(methods, function(i, method){
			var index = map.indexOf(method) + 1; // index of the deferred method name

			// copy the deferred method to an obj.method
			// 'ajax' text is trimmed from those names that contain it (prettier interface)
			obj[method.split('ajax').pop().toLowerCase()] = d[map[index]];
		});

		return obj;
	};

	return Popper;
});