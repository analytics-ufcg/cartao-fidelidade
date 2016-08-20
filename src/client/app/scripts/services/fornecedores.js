(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .factory('Fornecedores', fornecedores);

  fornecedores.$inject = ['RESTAPI','$resource'];

  /*jshint latedef: nofunc */
  function fornecedores(RESTAPI, $resource) {
    return $resource(RESTAPI.url+'/fornecedores/:id');
  }
})();
