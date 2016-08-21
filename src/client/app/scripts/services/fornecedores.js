(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .factory('Fornecedores', fornecedores);

  fornecedores.$inject = ['RESTAPI','$resource'];

  /*jshint latedef: nofunc */
  function fornecedores(RESTAPI, $resource) {
    var resource = {
      ranked: $resource(RESTAPI.url+'/ranked/fornecedores/:ano/:tipo'),
      simples: $resource(RESTAPI.url+'/fornecedores/:cpfCnpj/:tipo')
    };
    return resource;
  }
})();
