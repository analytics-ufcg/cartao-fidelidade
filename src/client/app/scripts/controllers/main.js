(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('MainCtrl', MainCtrl);

  MainCtrl.$inject = ['Fornecedores'];

  /*jshint latedef: nofunc */
  function MainCtrl(Fornecedores) {
    var vm = this;
    vm.fornecedores = Fornecedores.query();
  }
})();
